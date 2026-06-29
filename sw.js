const CACHE_NAME = 'console-ia-cache-v9';
const urlsToCache = [
  './',
  './index.html',
  './manifest.json?v=6',
  './app/src/main/res/drawable/synapse_logo_1781452080476.jpg'
];

self.addEventListener('install', event => {
  self.skipWaiting();
  event.waitUntil(
    caches.open(CACHE_NAME)
      .then(cache => cache.addAll(urlsToCache))
  );
});

self.addEventListener('activate', event => {
  event.waitUntil(
    caches.keys().then(cacheNames => {
      return Promise.all(
        cacheNames.map(cacheName => {
          if (cacheName !== CACHE_NAME) {
            return caches.delete(cacheName);
          }
        })
      );
    })
  );
  self.clients.claim();
});

self.addEventListener('fetch', event => {
  // Ignora chamadas de API externas e requisições que não sejam GET para evitar erros de CORS ou rede (ex: "Failed to fetch")
  if (event.request.method !== 'GET' || !event.request.url.startsWith(self.location.origin)) {
    return;
  }
  
  // Network-first strategy para recursos locais estáticos
  event.respondWith(
    fetch(event.request).catch(() => caches.match(event.request))
  );
});
