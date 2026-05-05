import{i as a}from"./index-242f2ae4.js";const t={list:async(t=1,s=10)=>(await a.get("/api/v1/transactions",{params:{page:t,pageSize:s}})).data,getById:async t=>(await a.get(`/api/v1/transactions/${t}`)).data,refund:async(t,s,n)=>(await a.post("/api/v1/refunds",{transactionId:t,amount:s,reason:n})).data};export{t};
//# sourceMappingURL=transactions-6feb513c.js.map
