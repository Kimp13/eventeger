import { ExpirationPlugin } from 'workbox-expiration';
import { CacheableResponsePlugin } from 'workbox-cacheable-response';
import { registerRoute, setDefaultHandler } from 'workbox-routing';
import { CacheFirst, NetworkFirst, StaleWhileRevalidate } from 'workbox-strategies';

const MAX_AGE = 25920000; // 30d
const MAX_ENTRIES = 60;

registerRoute(
  ({ request }) => request.destination === 'image',
  new CacheFirst({
    cacheName: 'images',
    plugins: [
      new CacheableResponsePlugin({
        statuses: [0, 200]
      }),

      new ExpirationPlugin({
        maxEntries: MAX_ENTRIES,
        maxAgeSeconds: MAX_AGE
      })
    ]
  })
);

registerRoute(
  /api\/announcements/,
  new NetworkFirst()
);

setDefaultHandler(new StaleWhileRevalidate({
  cacheName: 'default',
  plugins: [
    new ExpirationPlugin({
      maxAgeSeconds: MAX_AGE
    })
  ]
}));

self.addEventListener('push', function (event) {
  const data = event.data.json();
  const name = (
    data.author.firstName ?
      data.author.lastName ?
        data.author.firstName + ' ' + data.author.lastName :
        `${data.author.firstName} (${data.author.username})` :
      data.author.username
  );

  if (data.author.firstName) {
    if (data.author.lastName) {

    } else {

    }
  } else {

  }

  const options = {
    body: data.text,
    vibrate: [100, 50, 100],
    data: {
      dateOfArrival: Date.now(),
      primaryKey: data.id
    },
    actions: [
    ]
  };

  event.waitUntil(
    self.registration.showNotification(
      `${name} передаёт Вам привет!`,
      options
    )
  );
});

