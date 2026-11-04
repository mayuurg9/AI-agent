const fs = require("node:fs/promises");
const path = require("node:path");
const storeFile = path.join(__dirname, "../data/workspace.json");
async function store() {
  await fs.mkdir(path.dirname(storeFile), {recursive:true});
  try { return JSON.parse(await fs.readFile(storeFile,"utf8")); }
  catch { return {notes:[],tasks:[]}; }
}
async function save(data) { await fs.writeFile(storeFile,JSON.stringify(data,null,2)); }
const tools = [
 {type:"function",function:{name:"calculator",description:"Calculate basic arithmetic",parameters:{type:"object",properties:{expression:{type:"string"}},required:["expression"]}}},
 {type:"function",function:{name:"save_note",description:"Save a note to workspace memory",parameters:{type:"object",properties:{title:{type:"string"},content:{type:"string"}},required:["title","content"]}}},
 {type:"function",function:{name:"list_notes",description:"List saved notes",parameters:{type:"object",properties:{}}}},
 {type:"function",function:{name:"create_task",description:"Create a workspace task",parameters:{type:"object",properties:{title:{type:"string"},details:{type:"string"}},required:["title"]}}},
 {type:"function",function:{name:"list_tasks",description:"List workspace tasks",parameters:{type:"object",properties:{}}}}
];
function calc(s) {
 if(typeof s!=="string" || !/^[0-9+\-*/().%\s]+$/.test(s) || s.length>100) throw Error("Only basic arithmetic is supported.");
 const tokens=s.match(/\d+(?:\.\d+)?|[()+\-*/%]/g)||[]; let p=0;
 function atom(){let t=tokens[p++];if(t==="-"||t==="+")return (t==="-"?-1:1)*atom();if(t==="("){let v=sum();if(tokens[p++]!==")")throw Error("Missing )");return v;}let n=Number(t);if(!Number.isFinite(n))throw Error("Invalid expression");return n;}
 function product(){let v=atom();while(["*","/","%"].includes(tokens[p])){let op=tokens[p++],b=atom();if((op==="/"||op==="%")&&b===0)throw Error("Division by zero");v=op==="*"?v*b:op==="/"?v/b:v%b;}return v;}
 function sum(){let v=product();while(tokens[p]==="+"||tokens[p]==="-"){let op=tokens[p++],b=product();v=op==="+"?v+b:v-b;}return v;}
 let v=sum();if(p!==tokens.length||!Number.isFinite(v))throw Error("Invalid expression");return v;
}
async function runTool(name,args){
 const db=await store();
 if(name==="calculator")return {result:calc(args.expression)};
 if(name==="save_note"){let n={id:crypto.randomUUID(),title:String(args.title).slice(0,120),content:String(args.content).slice(0,4000),createdAt:new Date().toISOString()};db.notes.unshift(n);await save(db);return n;}
 if(name==="list_notes")return db.notes.slice(0,50);
 if(name==="create_task"){let t={id:crypto.randomUUID(),title:String(args.title).slice(0,160),details:String(args.details||"").slice(0,1000),done:false,createdAt:new Date().toISOString()};db.tasks.unshift(t);await save(db);return t;}
 if(name==="list_tasks")return db.tasks.slice(0,100);
 throw Error("Tool unavailable");
}
const providers={
 gemini:{key:()=>process.env.GEMINI_API_KEY,url:"https://generativelanguage.googleapis.com/v1beta/openai/chat/completions",model:"gemini-2.5-flash"},
 groq:{key:()=>process.env.GROQ_API_KEY,url:"https://api.groq.com/openai/v1/chat/completions",model:"llama-3.3-70b-versatile"},
 openrouter:{key:()=>process.env.OPENROUTER_API_KEY,url:"https://openrouter.ai/api/v1/chat/completions",model:"meta-llama/llama-3.3-70b-instruct:free"}
};
async function ask({message,history=[],provider="gemini",model}){
 const cfg=providers[provider];if(!cfg)throw Error("Unsupported provider.");
 const key=cfg.key();if(!key)throw Error("Missing "+provider.toUpperCase()+"_API_KEY in server environment.");
 const msgs=[{role:"system",content:"You are NEXUS, a practical AI agent. Use tools when useful. Explain actions accurately. Never claim a tool succeeded without a successful result. You cannot browse the web or execute arbitrary code."},...history.slice(-16),{role:"user",content:message}];
 const events=[];
 for(let step=0;step<5;step++){
  const response=await fetch(cfg.url,{method:"POST",headers:{Authorization:"Bearer "+key,"Content-Type":"application/json"},body:JSON.stringify({model:model||cfg.model,messages:msgs,tools,tool_choice:"auto"}),signal:AbortSignal.timeout(60000)});
  const body=await response.json().catch(()=>({}));if(!response.ok)throw Error(body?.error?.message||"Provider error "+response.status);
  const m=body.choices?.[0]?.message;if(!m)throw Error("Empty response from provider.");msgs.push(m);
  if(!m.tool_calls?.length)return {reply:m.content||"No text response.",events,model:model||cfg.model};
  for(const c of m.tool_calls){try{let args=JSON.parse(c.function.arguments||"{}"),result=await runTool(c.function.name,args);events.push({name:c.function.name,ok:true});msgs.push({role:"tool",tool_call_id:c.id,content:JSON.stringify(result)});}catch(e){events.push({name:c.function.name,ok:false});msgs.push({role:"tool",tool_call_id:c.id,content:JSON.stringify({error:e.message})});}}
 }
 return {reply:"I reached the tool-step limit. Ask me to continue.",events};
}
module.exports={ask,store};
