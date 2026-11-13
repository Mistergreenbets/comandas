const C='comandas-v24',L='libs-v1',A=['./','./index.html','./manifest.json','./icon-192.png','./icon-512.png'];
self.addEventListener('install',e=>{e.waitUntil(caches.open(C).then(c=>c.addAll(A)));self.skipWaiting()});
self.addEventListener('activate',e=>{e.waitUntil(caches.keys().then(k=>Promise.all(k.filter(x=>x!=C&&x!=L).map(x=>caches.delete(x)))));self.clients.claim()});
self.addEventListener('fetch',e=>{const r=e.request;if(r.method!='GET')return;
 const u=new URL(r.url);
 if(u.origin===location.origin&&(u.pathname.endsWith('/versao.json')||u.pathname.endsWith('/modelos/lista.json'))){e.respondWith(fetch(r).catch(()=>new Response('{}',{headers:{'Content-Type':'application/json'}})));return}
 if(u.origin===location.origin&&u.pathname.includes('/modelos/')){e.respondWith(fetch(r).then(res=>{const cp=res.clone();caches.open(C).then(c=>c.put(r,cp));return res}).catch(()=>caches.match(r)));return}
 if(u.hostname=='www.gstatic.com'&&u.pathname.startsWith('/firebasejs/')){
  e.respondWith(caches.open(L).then(c=>c.match(r).then(m=>m||fetch(r).then(res=>{c.put(r,res.clone());return res}))));return}
 if(u.origin!==location.origin&&!['image','font','style'].includes(r.destination))return;
 e.respondWith(caches.match(r).then(m=>m||fetch(r).then(res=>{const cp=res.clone();caches.open(C).then(c=>c.put(r,cp));return res}).catch(()=>caches.match('./index.html'))))});
