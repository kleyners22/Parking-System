/* Modulo operativo - consume la API REST con HTTP Basic (usar JWT en produccion) */
const API = '/api';
let auth = sessionStorage.getItem('auth');   // "Basic xxxx"
let chart;
const $ = id => document.getElementById(id);

async function api(path, method = 'GET', body) {
  const res = await fetch(API + path, {
    method,
    headers: { 'Content-Type': 'application/json', ...(auth ? { Authorization: auth } : {}) },
    body: body ? JSON.stringify(body) : undefined
  });
  if (res.status === 401) { logout(); throw new Error('Sesión inválida o usuario inactivo'); }
  const data = await res.json().catch(() => ({}));
  if (!res.ok) throw new Error(data.message || data.error || `Error ${res.status}`);
  return data;
}

function notify(msg, type = 'success') {
  $('alertBox').innerHTML = `<div class="alert alert-${type} alert-dismissible">${msg}
    <button class="btn-close" data-bs-dismiss="alert" onclick="this.parentElement.remove()"></button></div>`;
}

/* ---------- Login ---------- */
async function login() {
  auth = 'Basic ' + btoa(`${$('username').value}:${$('password').value}`);
  try {
    const me = await api('/auth/me');
    sessionStorage.setItem('auth', auth);
    $('userInfo').textContent = `${me.username} (${me.role})`;
    showApp();
  } catch (e) { auth = null; $('loginError').textContent = e.message; }
}
function logout() {
  sessionStorage.removeItem('auth'); auth = null;
  $('loginView').classList.remove('d-none'); $('appView').classList.add('d-none');
  $('btnLogout').classList.add('d-none');
}

/* ---------- Catalogos ---------- */
const opt = (items, label) => items.map(i => `<option value="${i.id}">${label(i)}</option>`).join('');
let accessPoints = [];

async function loadCatalogs() {
  const [locs, types, aps] = await Promise.all([api('/locations'), api('/vehicle-types'), api('/access-points')]);
  accessPoints = aps;
  $('location').innerHTML = opt(locs, l => l.name);
  $('vehicleType').innerHTML = opt(types, t => t.type);
  $('exitAp').innerHTML = opt(aps.filter(a => /SALIDA/i.test(a.type)), a => `${a.name} (${a.vehicleType.type})`);
  filterEntryAps();
}
function filterEntryAps() {
  const vt = +$('vehicleType').value;   // RF-14: el punto de acceso debe coincidir con el tipo de vehiculo
  $('entryAp').innerHTML = opt(
    accessPoints.filter(a => /ENTRADA/i.test(a.type) && a.vehicleType.id === vt), a => a.name);
}

/* ---------- Operaciones ---------- */
async function registerEntry() {
  try {
    const t = await api('/transactions/entry', 'POST', {
      plate: $('plate').value,
      locationId: +$('location').value,
      vehicleTypeId: +$('vehicleType').value,
      accessPointId: +$('entryAp').value,
      entryImage: null
    });
    $('qrBox').innerHTML = `<img src="${t.qrCode}" width="180" alt="QR"><div class="small mt-1">${t.code}</div>
      ${t.isMember ? '<span class="badge bg-info">Tarifa ' + t.tariffType + '</span>' : ''}`;
    $('plate').value = '';
    notify(`Entrada registrada: ${t.code}`);
    refresh();
  } catch (e) { notify(e.message, 'danger'); }
}

async function registerExit(code = $('exitCode').value.trim()) {
  if (!code) return notify('Ingrese el código de transacción', 'warning');
  try {
    const t = await api(`/transactions/${encodeURIComponent(code)}/exit`, 'POST',
      { accessPointId: +$('exitAp').value, exitImage: null });
    $('exitResult').innerHTML = `<div class="alert alert-info mb-0">
      <b>${t.plate}</b><br>Permanencia: ${Math.floor(t.durationMin / 60)} h ${t.durationMin % 60} min<br>
      Tarifa: ${t.tariffType}<br><span class="fs-4">Total: $${Number(t.amount).toFixed(2)}</span></div>`;
    $('exitCode').value = '';
    refresh();
  } catch (e) { notify(e.message, 'danger'); }
}

async function cancelTx(code) {
  if (!confirm('¿Cancelar la transacción ' + code + '?')) return;
  try { await api(`/transactions/${encodeURIComponent(code)}/cancel`, 'POST'); notify('Transacción cancelada', 'secondary'); refresh(); }
  catch (e) { notify(e.message, 'danger'); }
}

/* ---------- Dashboard ---------- */
async function refresh() {
  const [active, avail] = await Promise.all([api('/transactions/active'), api('/parking/availability')]);
  $('activeBody').innerHTML = active.map(t => `<tr>
    <td class="small">${t.code}</td><td>${t.plate}</td><td>${t.vehicleType}</td>
    <td>${new Date(t.entryTime).toLocaleString()}</td><td>${t.tariffType ?? ''}</td>
    <td class="text-nowrap">
      <button class="btn btn-sm btn-outline-primary" onclick="$('exitCode').value='${t.code}'">Salida</button>
      <button class="btn btn-sm btn-outline-danger" onclick="cancelTx('${t.code}')">Cancelar</button>
    </td></tr>`).join('');

  const labels = avail.map(a => `${a.location} · ${a.vehicleType}`);
  const data = {
    labels,
    datasets: [
      { label: 'Ocupados', data: avail.map(a => a.occupied), backgroundColor: '#dc3545' },
      { label: 'Libres',   data: avail.map(a => a.free),     backgroundColor: '#198754' }
    ]
  };
  if (chart) { chart.data = data; chart.update(); }
  else chart = new Chart($('occupancyChart'), { type: 'bar', data,
    options: { scales: { x: { stacked: true }, y: { stacked: true, beginAtZero: true } } } });
}

async function showApp() {
  $('loginView').classList.add('d-none'); $('appView').classList.remove('d-none');
  $('btnLogout').classList.remove('d-none');
  await loadCatalogs(); await refresh();
  setInterval(() => auth && refresh().catch(() => {}), 15000);
}

$('btnLogin').onclick = login;
$('btnLogout').onclick = logout;
$('btnEntry').onclick = registerEntry;
$('btnExit').onclick = () => registerExit();
$('vehicleType').onchange = filterEntryAps;
if (auth) api('/auth/me').then(me => { $('userInfo').textContent = `${me.username} (${me.role})`; showApp(); }).catch(logout);
