import client from './client';

export function createWxQrSession() {
  return client.post('/auth/wx/qr/create');
}

export function pollWxQrSession(state) {
  return client.get('/auth/wx/qr/poll', { params: { state } });
}

export function devConfirmWxQr(state) {
  return client.post('/auth/wx/qr/dev-confirm', { state });
}
