const CACHE_NAME = 'console-ia-cache-v24';
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

// Suporte a Notificações Push e Interações em segundo plano
self.addEventListener('push', event => {
  let data = { title: 'Synapse Console', body: 'Nova atualização do sistema.', icon: './app/src/main/res/drawable/synapse_logo_1781452080476.jpg' };
  if (event.data) {
    try {
      data = event.data.json();
    } catch (e) {
      data.body = event.data.text();
    }
  }

  const options = {
    body: data.body || 'Alerta do console',
    icon: data.icon || './app/src/main/res/drawable/synapse_logo_1781452080476.jpg',
    badge: data.badge || './app/src/main/res/drawable/synapse_logo_1781452080476.jpg',
    vibrate: [100, 50, 100],
    data: {
      url: data.url || './index.html'
    }
  };

  event.waitUntil(
    self.registration.showNotification(data.title || 'Synapse Console', options)
  );
});

self.addEventListener('notificationclick', event => {
  event.notification.close();
  event.waitUntil(
    clients.matchAll({ type: 'window', includeUncontrolled: true }).then(clientList => {
      for (const client of clientList) {
        if (client.url.includes('index.html') && 'focus' in client) {
          return client.focus();
        }
      }
      if (clients.openWindow) {
        return clients.openWindow('./index.html');
      }
    })
  );
});

// Permite que o app envie comandos de exibicao de notificacao para o Service Worker em segundo plano
self.addEventListener("message", event => {
  if (event.data && event.data.type === "SHOW_NOTIFICATION") {
    const d = event.data;
    const options = {
      body: d.message || d.body || "Nova mensagem da Inteligência Artificial",
      icon: d.icon || "./app/src/main/res/drawable/synapse_logo_1781452080476.jpg",
      badge: d.badge || "./app/src/main/res/drawable/synapse_logo_1781452080476.jpg",
      vibrate: [200, 100, 200],
      tag: d.tag || ("synapse-ai-notif-" + Date.now()),
      renotify: true,
      requireInteraction: false,
      data: {
        url: "./index.html"
      }
    };
    event.waitUntil(
      self.registration.showNotification(d.title || "⚡ Synapse AI Console", options)
    );
  }
});
