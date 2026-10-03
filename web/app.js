/* ============================================
   ParkWise Frontend Application Logic
   ============================================ */

const isLocal = window.location.hostname === 'localhost' || window.location.protocol === 'file:' || window.location.hostname === '127.0.0.1';
let API = isLocal ? 'http://localhost:8080/api' : '/api';
if (!isLocal && typeof CONFIG !== 'undefined' && CONFIG.API_URL) {
    API = CONFIG.API_URL;
}
let currentUser = null;
let exitCurrentRecord = null;
let editingVehicleId = null;
let confirmCallback = null;

// ==================== INIT ====================

window.onload = () => {
  const saved = sessionStorage.getItem('parkwise_user');
  if (saved) {
    currentUser = JSON.parse(saved);
    startApp();
  }
  startClock();
};

function startClock() {
  setInterval(() => {
    const el = document.getElementById('clockDisplay');
    if (el) el.textContent = '🕐 ' + new Date().toLocaleString('en-IN', {weekday:'short', day:'2-digit', month:'short', year:'numeric', hour:'2-digit', minute:'2-digit'});
  }, 1000);
}

// ==================== UI STATE ====================

function toggleSidebar() {
  const sidebar = document.getElementById('sidebar');
  const overlay = document.getElementById('mobileOverlay');
  if (sidebar.classList.contains('open')) {
    sidebar.classList.remove('open');
    overlay.classList.remove('show');
  } else {
    sidebar.classList.add('open');
    overlay.classList.add('show');
  }
}

function showConfirmModal(title, message, btnClass, callback) {
  document.getElementById('confirmTitle').textContent = title;
  document.getElementById('confirmMessage').innerHTML = message;
  const okBtn = document.getElementById('confirmOkBtn');
  okBtn.className = 'btn ' + btnClass;
  confirmCallback = callback;
  document.getElementById('confirmModal').classList.remove('hidden');
}

function closeConfirmModal() {
  document.getElementById('confirmModal').classList.add('hidden');
  confirmCallback = null;
}

document.getElementById('confirmOkBtn').addEventListener('click', () => {
  if (confirmCallback) confirmCallback();
  closeConfirmModal();
});

// ==================== AUTH ====================

async function handleLogin() {
  const btn = document.getElementById('loginBtn');
  const username = document.getElementById('loginUsername').value.trim();
  const password = document.getElementById('loginPassword').value;
  const errEl   = document.getElementById('loginError');

  errEl.classList.add('hidden');
  if (!username || !password) { showEl(errEl, '⚠️ Enter both username and password.'); return; }

  const ogText = btn.innerHTML;
  btn.innerHTML = '<div class="spinner"></div>'; btn.disabled = true;

  try {
    console.log('--- FINAL DEBUG ---');
    console.log('Initiating login request...');
    console.log('Using API base URL:', API);
    console.log('Full URL being fetched:', API + '/login');
    
    const res = await post('/login', { username, password });
    if (res.success) {
      currentUser = { username: res.username, fullName: res.fullName };
      sessionStorage.setItem('parkwise_user', JSON.stringify(currentUser));
      setTimeout(() => startApp(), 300); // Small delay for UX
    } else {
      showEl(errEl, '❌ ' + res.message);
    }
  } catch (e) {
    console.error('Fetch exception occurred:', e);
    showEl(errEl, '❌ Cannot connect to server. Is the Java backend running? (See console for details)');
  } finally {
    btn.innerHTML = ogText; btn.disabled = false;
  }
}

let dashboardTimer;
function startApp() {
  document.getElementById('loginPage').classList.add('hidden');
  document.getElementById('loginPage').classList.remove('active');
  document.getElementById('appPage').classList.remove('hidden');
  document.getElementById('sidebarAdmin').textContent = currentUser.fullName || currentUser.username;
  showPanel('dashboard');
  if (dashboardTimer) clearInterval(dashboardTimer);
  dashboardTimer = setInterval(() => { if (document.getElementById('panel-dashboard').classList.contains('active')) loadDashboard(); }, 15000); // Poll every 15s
}

function handleLogout() {
  showConfirmModal('Logout', 'Are you sure you want to logout?', 'btn-danger', () => {
    currentUser = null;
    sessionStorage.removeItem('parkwise_user');
    document.getElementById('appPage').classList.add('hidden');
    document.getElementById('loginPage').classList.remove('hidden');
    document.getElementById('loginPage').classList.add('active');
    document.getElementById('loginUsername').value = '';
    document.getElementById('loginPassword').value = '';
  });
}

// ==================== NAVIGATION ====================

const panelTitles = {
  dashboard: 'Dashboard',
  entry:     'Vehicle Entry',
  exit:      'Vehicle Exit',
  slots:     'Parking Slots',
  history:   'Parking History',
  reports:   'Reports & Analytics',
  vehicles:  'Vehicle Management'
};

function showPanel(name) {
  document.querySelectorAll('.panel').forEach(p => {
    p.classList.remove('active');
    p.classList.add('hidden');
  });
  document.querySelectorAll('.nav-item').forEach(n => n.classList.remove('active'));
  const target = document.getElementById('panel-' + name);
  target.classList.remove('hidden');
  target.classList.add('active');
  document.querySelector(`[data-panel="${name}"]`).classList.add('active');
  document.getElementById('topbarTitle').textContent = panelTitles[name] || name;

  // Close mobile sidebar if open
  document.getElementById('sidebar').classList.remove('open');
  document.getElementById('mobileOverlay').classList.remove('show');

  // Load panel data
  if (name === 'dashboard') loadDashboard();
  if (name === 'slots')     loadSlots();
  if (name === 'history')   loadHistory();
  if (name === 'reports')   loadReports();
  if (name === 'vehicles')  loadVehicles();
  if (name === 'entry')     loadSlotBadges();
}

// ==================== DASHBOARD ====================

async function loadDashboard() {
  try {
    const data = await get('/dashboard');
    renderStats(data);
    renderParkedTable(data.parkedVehicles || []);
  } catch(e) { toast('Failed to load dashboard.', 'error'); }
}

function renderStats(d) {
  const g = document.getElementById('statsGrid');
  g.innerHTML = `
    ${statCard('🅿️', 'Total Slots',    d.totalSlots,   '#6366f1')}
    ${statCard('✅', 'Available',       d.available,    '#22c55e')}
    ${statCard('🔴', 'Occupied',        d.occupied,     '#ef4444')}
    ${statCard('🚗', 'Parked Now',      d.parkedNow,    '#38bdf8')}
    ${statCard('📥', "Today's Entries", d.todayEntries || 0, '#8b5cf6')}
    ${statCard('🚪', "Today's Exits",   d.todayExits || 0,   '#f97316')}
    ${statCard('💰', "Today's Revenue", '₹' + fmt(d.todayRevenue), '#f59e0b')}
    ${statCard('💎', 'Total Revenue',   '₹' + fmt(d.totalRevenue), '#a855f7')}
  `;
}

function statCard(icon, label, value, color) {
  return `<div class="stat-card" style="border-top-color:${color}">
    <div class="stat-icon">${icon}</div>
    <div class="stat-info"><div class="stat-label">${label}</div><div class="stat-value">${value}</div></div>
  </div>`;
}

function renderParkedTable(list) {
  const tb = document.getElementById('parkedTbody');
  if (!list.length) { tb.innerHTML = '<tr><td colspan="9" class="empty">No vehicles currently parked.</td></tr>'; return; }
  tb.innerHTML = list.map(r => `
    <tr>
      <td><code>${r.ticketId}</code></td>
      <td><b>${r.vehicleNumber}</b></td>
      <td>${r.ownerName}</td>
      <td>${r.phone}</td>
      <td>${r.vehicleType === 'Car' ? '🚗' : '🏍️'} ${r.vehicleType}</td>
      <td><b>${r.slotNumber}</b></td>
      <td>${r.entryTime}</td>
      <td><span style="color:var(--warning);font-weight:600">₹${fmt(r.estimatedFee || 0)}</span></td>
      <td><button class="btn btn-sm btn-outline" style="color:var(--danger);border-color:var(--danger)" onclick="quickExit('${r.vehicleNumber}')">Exit</button></td>
    </tr>`).join('');
}

function quickExit(vehicleNum) {
  showPanel('exit');
  document.getElementById('exitSearch').value = vehicleNum;
  findVehicleForExit();
}

// ==================== VEHICLE ENTRY ====================

async function loadSlotBadges() {
  try {
    const data = await get('/dashboard');
    const container = document.getElementById('slotBadges');
    container.innerHTML = `
      <span class="badge" style="background:rgba(56,189,248,.15);color:#38bdf8;border:1px solid rgba(56,189,248,.3)">🏍️ Bikes: ${data.bikeAvail}/${data.bikeTotal} free</span>
      <span class="badge" style="background:rgba(99,102,241,.15);color:#6366f1;border:1px solid rgba(99,102,241,.3)">🚗 Cars: ${data.carAvail}/${data.carTotal} free</span>
      <span style="color:var(--text-muted);font-size:12px;margin-left:auto">Rates: Bike ₹${data.bikeRate}/hr · Car ₹${data.carRate}/hr</span>
    `;
  } catch(e) {}
}

function updateSlotBadges() { loadSlotBadges(); }

async function handleEntry() {
  const vehicleNumber = val('entryVehicleNum');
  const ownerName     = val('entryOwner');
  const phone         = val('entryPhone');
  const vehicleType   = val('entryType');
  const errEl         = document.getElementById('entryError');
  errEl.classList.add('hidden');

  if (!vehicleNumber || !ownerName || !phone) { showEl(errEl, '⚠️ All fields are required.'); return; }
  if (phone.length !== 10) { showEl(errEl, '⚠️ Phone number must be 10 digits.'); return; }

  const btn = document.getElementById('entryBtn');
  btn.disabled = true; btn.innerHTML = '<div class="spinner"></div> Parking...';

  try {
    const res = await post('/parking/entry', { vehicleNumber, ownerName, phone, vehicleType });
    if (res.success) {
      toast('✅ ' + res.message, 'success');
      showTicket(res);
      clearEntryForm();
      loadSlotBadges();
    } else {
      showEl(errEl, '❌ ' + res.message);
    }
  } catch(e) {
    showEl(errEl, '❌ ' + (e.message || 'Server error.'));
  } finally {
    btn.disabled = false; btn.innerHTML = '🅿️ Park Vehicle';
  }
}

function showTicket(r) {
  document.getElementById('ticketContent').innerHTML = `
    <div class="ticket-box">
      <div class="ticket-header">
        <h3>🎫 PARKING TICKET</h3>
        <p>ParkWise Management System</p>
      </div>
      <hr class="ticket-sep"/>
      ${tRow('Ticket ID', `<code>${r.ticketId}</code>`)}
      ${tRow('Vehicle No.', `<b style="font-size:16px">${r.vehicleNumber}</b>`)}
      ${tRow('Owner', r.ownerName)}
      ${tRow('Phone', r.phone)}
      ${tRow('Type', (r.vehicleType === 'Car' ? '🚗 ' : '🏍️ ') + r.vehicleType)}
      ${tRow('Slot', `<b style="font-size:16px;color:var(--primary)">${r.slotNumber}</b>`)}
      ${tRow('Entry Time', r.entryTime)}
      <hr class="ticket-sep"/>
      ${tRow('Rate', `₹${fmt(r.rate)} / hour`)}
      <div style="margin-top:16px;text-align:center">
        <span class="ticket-paid">✅ VEHICLE PARKED</span>
      </div>
    </div>`;
}

function clearEntryForm() {
  ['entryVehicleNum','entryOwner','entryPhone'].forEach(id => document.getElementById(id).value = '');
  document.getElementById('entryType').selectedIndex = 0;
  document.getElementById('entryError').classList.add('hidden');
}

// ==================== VEHICLE EXIT ====================

async function findVehicleForExit() {
  const q = val('exitSearch');
  if (!q) { toast('Enter a ticket ID or vehicle number.', 'error'); return; }

  const btn = document.getElementById('exitFindBtn');
  const ogText = btn.innerHTML;
  btn.innerHTML = '<div class="spinner"></div>'; btn.disabled = true;

  try {
    const res = await get('/parking/find?q=' + encodeURIComponent(q));
    exitCurrentRecord = res;
    showExitVehicleInfo(res);
  } catch(e) {
    toast(e.message || 'No active record found.', 'error');
    document.getElementById('exitVehicleInfo').classList.add('hidden');
    document.getElementById('exitEmptyMsg').classList.remove('hidden');
  } finally {
    btn.innerHTML = ogText; btn.disabled = false;
  }
}

function showExitVehicleInfo(r) {
  document.getElementById('exitEmptyMsg').classList.add('hidden');
  document.getElementById('exitVehicleInfo').classList.remove('hidden');
  const h = Math.floor(r.currentMinutes / 60), m = r.currentMinutes % 60;
  const durStr = h > 0 ? `${h}h ${m}m` : `${m}m`;
  document.getElementById('exitDetailsContent').innerHTML = `
    ${tRow('Ticket ID', `<code>${r.ticketId}</code>`)}
    ${tRow('Vehicle No.', `<b>${r.vehicleNumber}</b>`)}
    ${tRow('Owner', r.ownerName)}
    ${tRow('Type', r.vehicleType)}
    ${tRow('Slot', r.slotNumber)}
    ${tRow('Entry Time', r.entryTime)}
    ${tRow('Duration', `<b>${durStr}</b>`)}
    ${tRow('Estimated Fee', `<b style="color:var(--warning);font-size:16px">₹${fmt(r.estimatedFee)}</b>`)}
  `;
  document.getElementById('exitError').classList.add('hidden');
}

async function handleExit() {
  if (!exitCurrentRecord) return;
  const identifier     = exitCurrentRecord.ticketId;
  const paymentMethod  = val('exitPaymentMethod');
  const errEl = document.getElementById('exitError');
  errEl.classList.add('hidden');

  showConfirmModal('Process Exit', `Process exit for <b>${exitCurrentRecord.vehicleNumber}</b>?<br><br>Estimated fee: <b style="color:var(--warning)">₹${fmt(exitCurrentRecord.estimatedFee)}</b><br>Payment Method: <b>${paymentMethod}</b>`, 'btn-success', async () => {
    
    const btn = document.getElementById('exitBtn');
    btn.disabled = true; btn.innerHTML = '<div class="spinner"></div> Processing...';

    try {
      const res = await post('/parking/exit', { identifier, paymentMethod });
      if (res.success) {
        toast('✅ Exit processed! Amount: ₹' + fmt(res.amount), 'success');
        showReceipt(res, paymentMethod);
        document.getElementById('exitVehicleInfo').classList.add('hidden');
        document.getElementById('exitEmptyMsg').classList.remove('hidden');
        document.getElementById('exitSearch').value = '';
        document.getElementById('printReceiptBtn').classList.remove('hidden');
        exitCurrentRecord = null;
      } else {
        showEl(errEl, '❌ ' + res.message);
      }
    } catch(e) {
      showEl(errEl, '❌ ' + (e.message || 'Server error.'));
    } finally {
      btn.disabled = false; btn.innerHTML = '🚪 Process Exit & Pay';
    }
  });
}

function showReceipt(r, payMethod) {
  document.getElementById('receiptContent').innerHTML = `
    <div class="ticket-box">
      <div class="ticket-header">
        <h3>🧾 PARKING RECEIPT</h3>
        <p>ParkWise Management System</p>
      </div>
      <hr class="ticket-sep"/>
      ${tRow('Ticket ID', `<code>${r.ticketId}</code>`)}
      ${tRow('Vehicle No.', `<b>${r.vehicleNumber}</b>`)}
      ${tRow('Owner', r.ownerName)}
      ${tRow('Type', r.vehicleType)}
      ${tRow('Slot', r.slotNumber)}
      <hr class="ticket-sep"/>
      ${tRow('Entry', r.entryTime)}
      ${tRow('Exit', r.exitTime)}
      ${tRow('Duration', r.duration)}
      ${tRow('Payment Method', payMethod)}
      <hr class="ticket-sep"/>
      <div class="ticket-total">
        <div style="font-size:12px;color:var(--text-muted);font-weight:600;letter-spacing:1px">TOTAL AMOUNT</div>
        <div class="amount">₹${fmt(r.amount)}</div>
        <span class="ticket-paid">✅ PAID</span>
      </div>
    </div>`;
}

function printReceipt() {
  const content = document.getElementById('receiptContent').innerHTML;
  const win = window.open('', '_blank');
  win.document.write(`
    <html>
      <head>
        <title>Parking Receipt</title>
        <style>
          body { font-family: 'Helvetica Neue', Helvetica, Arial, sans-serif; padding: 20px; text-align: center; color: #333; max-width: 400px; margin: 0 auto; }
          .ticket-row { display: flex; justify-content: space-between; margin: 10px 0; border-bottom: 1px dashed #ddd; padding-bottom: 8px; font-size: 14px; }
          .ticket-total { font-size: 28px; font-weight: bold; margin-top: 20px; color: #000; }
          .ticket-sep { border: none; border-top: 2px dashed #000; margin: 20px 0; }
          .ticket-paid { display: inline-block; background: #e6f4ea; color: #137333; padding: 6px 20px; border-radius: 20px; font-size: 14px; font-weight: bold; border: 1px solid #ceead6; margin-top: 10px; }
          h3 { margin: 0; font-size: 22px; }
          p { margin: 5px 0 15px; color: #666; font-size: 12px; }
          code { font-family: monospace; background: #f0f0f0; padding: 2px 6px; border-radius: 4px; }
        </style>
      </head>
      <body onload="window.print(); setTimeout(()=>window.close(), 500);">
        ${content}
      </body>
    </html>
  `);
}

// ==================== PARKING SLOTS ====================

async function loadSlots() {
  const type = val('slotFilter') || 'All';
  try {
    const slots = await get('/slots?type=' + type);
    const dash  = await get('/dashboard');
    renderSlots(slots, dash.parkedVehicles || []);
    document.getElementById('slotCountBadges').innerHTML = `
      <span class="badge" style="background:rgba(56,189,248,.1);color:#38bdf8">🏍️ Bikes: ${dash.bikeAvail}/${dash.bikeTotal}</span>
      <span class="badge" style="background:rgba(99,102,241,.1);color:#6366f1">🚗 Cars: ${dash.carAvail}/${dash.carTotal}</span>`;
  } catch(e) { toast('Failed to load slots.', 'error'); }
}

function renderSlots(slots, parkedVehicles) {
  const g = document.getElementById('slotsGrid');
  g.innerHTML = slots.map(s => {
    const avail = s.status === 'Available';
    const icon  = s.slotType === 'Bike' ? '🏍️' : '🚗';
    const parked = parkedVehicles.find(p => p.slotNumber === s.slotNumber);
    return `<div class="slot-tile ${avail ? 'slot-available' : 'slot-occupied'}" style="padding: 12px; height: 100px; display: flex; flex-direction: column; justify-content: center; position: relative;">
      <div style="font-size:18px; font-weight:700; color:var(--text-bright); display:flex; justify-content: space-between; align-items: center; margin-bottom: 5px;">
        <span>${s.slotNumber}</span>
        <span>${icon}</span>
      </div>
      <div class="slot-status" style="font-size: 11px; margin-bottom: 5px;">${s.status}</div>
      ${parked ? `<div style="font-size:12px; font-weight:600; color: var(--warning)">${parked.vehicleNumber}</div>
                  <div style="font-size:10px; color: var(--text-muted)">${parked.entryTime.split(' ')[1] || parked.entryTime}</div>` 
                : `<div style="font-size:12px; color: var(--text-muted)">Empty</div>`}
    </div>`;
  }).join('');
}

// ==================== HISTORY ====================

async function loadHistory() {
  const q    = document.getElementById('histSearch').value.trim();
  const from = document.getElementById('histFrom').value;
  const to   = document.getElementById('histTo').value;

  let url = '/parking/history?';
  if (q)    url += 'q='    + encodeURIComponent(q) + '&';
  if (from) url += 'from=' + from + '&';
  if (to)   url += 'to='   + to;

  try {
    const records = await get(url);
    const tb = document.getElementById('historyTbody');
    if (!records.length) {
      tb.innerHTML = '<tr><td colspan="10" class="empty">No records found matching criteria.</td></tr>';
      return;
    }
    tb.innerHTML = records.map(r => `
      <tr>
        <td><code>${r.ticketId}</code></td>
        <td><b>${r.vehicleNumber}</b></td>
        <td>${r.ownerName}</td>
        <td>${r.vehicleType === 'Car' ? '🚗' : '🏍️'} ${r.vehicleType}</td>
        <td><b>${r.slotNumber}</b></td>
        <td>${r.entryTime}</td>
        <td>${r.exitTime || '<span style="color:var(--text-muted)">—</span>'}</td>
        <td>${r.duration || '<span style="color:var(--text-muted)">—</span>'}</td>
        <td>${r.amount ? '<b style="color:var(--success)">₹' + fmt(r.amount) + '</b>' : '<span style="color:var(--text-muted)">—</span>'}</td>
        <td><span class="badge badge-${r.status.toLowerCase()}">${r.status}</span></td>
      </tr>`).join('');
  } catch(e) { toast('Failed to load history.', 'error'); }
}

function resetHistory() {
  document.getElementById('histSearch').value = '';
  document.getElementById('histFrom').value   = '';
  document.getElementById('histTo').value     = '';
  loadHistory();
}

function exportHistoryCSV() {
  const rows = document.querySelectorAll('#historyTable tr');
  let csv = [];
  for (let i = 0; i < rows.length; i++) {
    let row = [], cols = rows[i].querySelectorAll('td, th');
    for (let j = 0; j < cols.length; j++) {
      let data = cols[j].innerText.replace(/"/g, '""');
      row.push('"' + data + '"');
    }
    csv.push(row.join(','));
  }
  // Use proper newline for CSV
  const blob = new Blob([csv.join('\n')], { type: 'text/csv;charset=utf-8;' });
  const url = window.URL.createObjectURL(blob);
  const a = document.createElement('a');
  a.setAttribute('hidden', '');
  a.setAttribute('href', url);
  a.setAttribute('download', 'parking_history_' + new Date().toISOString().slice(0,10) + '.csv');
  document.body.appendChild(a);
  a.click();
  document.body.removeChild(a);
}

// ==================== REPORTS ====================

async function loadReports() {
  const from = document.getElementById('repFrom').value;
  const to   = document.getElementById('repTo').value;
  let url = '/reports';
  if (from && to) url += `?from=${from}&to=${to}`;
  
  try {
    const d = await get(url);
    document.getElementById('reportStatsGrid').innerHTML = `
      ${statCard('💰', 'Period Revenue', '₹' + fmt(d.totalRevenue), '#22c55e')}
      ${statCard('🏍️', 'Bike Revenue', '₹' + fmt(d.bikeRevenue), '#06b6d4')}
      ${statCard('🚗', 'Car Revenue', '₹' + fmt(d.carRevenue), '#f97316')}
      ${statCard('📊', 'Total Vehicles', d.totalCount, '#6366f1')}
      ${statCard('🏍️', 'Bikes Parked', d.bikeCount, '#38bdf8')}
      ${statCard('🚗', 'Cars Parked', d.carCount, '#f59e0b')}
      ${statCard('📥', "Today's Entries", d.todayEntries || 0, '#8b5cf6')}
      ${statCard('🚪', "Today's Exits", d.todayExits || 0, '#f97316')}
      ${statCard('💰', "Today's Revenue", '₹' + fmt(d.todayRevenue || 0), '#f59e0b')}
      ${statCard('💎', 'All-Time Revenue', '₹' + fmt(d.allTimeRevenue || 0), '#a855f7')}
      ${statCard('🅿️', 'Total Slots', d.totalSlots || 0, '#6366f1')}
      ${statCard('✅', 'Available', d.available || 0, '#22c55e')}
      ${statCard('🔴', 'Occupied', d.occupied || 0, '#ef4444')}
    `;
    
    document.getElementById('revenueBreakdown').innerHTML = `
      <div style="display:flex;justify-content:space-between;border-bottom:1px solid var(--border);padding:8px 0"><span>Total Revenue:</span> <b>₹${fmt(d.totalRevenue)}</b></div>
      <div style="display:flex;justify-content:space-between;border-bottom:1px solid var(--border);padding:8px 0"><span>Bike Revenue:</span> <span>₹${fmt(d.bikeRevenue)} <span style="color:var(--text-muted);font-size:12px">(${d.totalRevenue > 0 ? (d.bikeRevenue*100/d.totalRevenue).toFixed(1) : 0}%)</span></span></div>
      <div style="display:flex;justify-content:space-between;border-bottom:1px solid var(--border);padding:8px 0"><span>Car Revenue:</span> <span>₹${fmt(d.carRevenue)} <span style="color:var(--text-muted);font-size:12px">(${d.totalRevenue > 0 ? (d.carRevenue*100/d.totalRevenue).toFixed(1) : 0}%)</span></span></div>
      <div style="display:flex;justify-content:space-between;padding:8px 0"><span>Avg. Revenue/Vehicle:</span> <b style="color:var(--success)">₹${fmt(d.totalCount > 0 ? d.totalRevenue / d.totalCount : 0)}</b></div>
    `;
    
    document.getElementById('vehicleBreakdown').innerHTML = `
      <div style="display:flex;justify-content:space-between;border-bottom:1px solid var(--border);padding:8px 0"><span>Total Vehicles:</span> <b>${d.totalCount}</b></div>
      <div style="display:flex;justify-content:space-between;border-bottom:1px solid var(--border);padding:8px 0"><span>Bikes:</span> <span>${d.bikeCount} <span style="color:var(--text-muted);font-size:12px">(${d.totalCount > 0 ? (d.bikeCount*100/d.totalCount).toFixed(1) : 0}%)</span></span></div>
      <div style="display:flex;justify-content:space-between;padding:8px 0"><span>Cars:</span> <span>${d.carCount} <span style="color:var(--text-muted);font-size:12px">(${d.totalCount > 0 ? (d.carCount*100/d.totalCount).toFixed(1) : 0}%)</span></span></div>
    `;
    
    const toYMD = (dStr) => {
      if (!dStr) return '';
      const p = dStr.split('-');
      if (p.length !== 3) return dStr;
      return `${p[2]}-${p[1]}-${p[0]}`;
    };
    if (d.from && d.to) {
      document.getElementById('repFrom').value = toYMD(d.from);
      document.getElementById('repTo').value = toYMD(d.to);
    }
  } catch (e) { toast('Failed to load reports.', 'error'); }
}

function quickReport(type) {
  const now = new Date();
  const ymd = (d) => {
    const dt = new Date(d);
    dt.setMinutes(dt.getMinutes() - dt.getTimezoneOffset());
    return dt.toISOString().split('T')[0];
  };
  let from = new Date(), to = new Date();
  
  if (type === 'today') {
    // Both from and to are today
  } else if (type === 'week') {
    from.setDate(now.getDate() - (now.getDay() || 7) + 1);
  } else if (type === 'month') {
    from = new Date(now.getFullYear(), now.getMonth(), 1);
  }
  
  document.getElementById('repFrom').value = ymd(from);
  document.getElementById('repTo').value = ymd(to);
  loadReports();
}

function printReport() {
  window.print();
}

// ==================== VEHICLE MANAGEMENT ====================

async function loadVehicles() {
  const q = document.getElementById('vSearch').value.trim();
  const url = q ? '/vehicles?q=' + encodeURIComponent(q) : '/vehicles';
  try {
    const list = await get(url);
    const tb = document.getElementById('vehicleTbody');
    if (!list.length) {
      tb.innerHTML = '<tr><td colspan="6" class="empty">No vehicles found.</td></tr>';
      return;
    }
    tb.innerHTML = list.map(v => `
      <tr>
        <td>${v.id}</td>
        <td><b>${v.vehicleNumber}</b></td>
        <td>${v.ownerName}</td>
        <td>${v.phone}</td>
        <td>${v.vehicleType === 'Car' ? '🚗' : '🏍️'} ${v.vehicleType}</td>
        <td>
          <button class="btn btn-sm btn-outline" onclick='editVehicle(${v.id},"${v.vehicleNumber}","${v.ownerName}","${v.phone}","${v.vehicleType}")'>✏️ Edit</button>
        </td>
      </tr>`).join('');
  } catch(e) { toast('Failed to load vehicles.', 'error'); }
}

function editVehicle(id, num, owner, phone, type) {
  editingVehicleId = id;
  document.getElementById('vNum').value   = num;
  document.getElementById('vOwner').value = owner;
  document.getElementById('vPhone').value = phone;
  document.getElementById('vType').value  = type;
  document.getElementById('vehicleFormTitle').innerHTML = '✏️ Edit Vehicle <span style="color:var(--text-muted)">#' + id + '</span>';
  document.getElementById('vSaveBtn').innerHTML = '💾 Update';
  document.getElementById('vDeleteBtn').classList.remove('hidden');
  document.getElementById('vError').classList.add('hidden');
}

async function saveVehicle() {
  const vehicleNumber = val('vNum');
  const ownerName     = val('vOwner');
  const phone         = val('vPhone');
  const vehicleType   = val('vType');
  const errEl         = document.getElementById('vError');
  errEl.classList.add('hidden');

  if (!vehicleNumber || !ownerName || !phone) { showEl(errEl, '⚠️ All fields required.'); return; }
  if (phone.length !== 10) { showEl(errEl, '⚠️ Phone number must be 10 digits.'); return; }

  const btn = document.getElementById('vSaveBtn');
  const ogText = btn.innerHTML;
  btn.innerHTML = '<div class="spinner"></div>'; btn.disabled = true;

  try {
    let res;
    if (editingVehicleId) {
      res = await put('/vehicles/' + editingVehicleId, { vehicleNumber, ownerName, phone, vehicleType });
    } else {
      res = await post('/vehicles', { vehicleNumber, ownerName, phone, vehicleType });
    }
    if (res.success) {
      toast('✅ ' + res.message, 'success');
      clearVehicleForm();
      loadVehicles();
    } else {
      showEl(errEl, '❌ ' + res.message);
    }
  } catch(e) {
    showEl(errEl, '❌ ' + (e.message || 'Server error.'));
  } finally {
    btn.innerHTML = ogText; btn.disabled = false;
  }
}

function deleteVehicle() {
  if (!editingVehicleId) return;
  showConfirmModal('Delete Vehicle', 'Are you sure you want to delete this vehicle record? This cannot be undone.', 'btn-danger', async () => {
    try {
      const res = await del('/vehicles/' + editingVehicleId);
      if (res.success) {
        toast('✅ Vehicle deleted successfully.', 'success');
        clearVehicleForm();
        loadVehicles();
      } else {
        toast('❌ ' + res.message, 'error');
      }
    } catch(e) { toast('❌ Delete failed.', 'error'); }
  });
}

function clearVehicleForm() {
  editingVehicleId = null;
  ['vNum','vOwner','vPhone'].forEach(id => document.getElementById(id).value = '');
  document.getElementById('vType').selectedIndex = 0;
  document.getElementById('vehicleFormTitle').textContent = '📝 Add Vehicle';
  document.getElementById('vSaveBtn').innerHTML = '➕ Add';
  document.getElementById('vDeleteBtn').classList.add('hidden');
  document.getElementById('vError').classList.add('hidden');
}

// ==================== HTTP HELPERS ====================

async function get(path) {
  const res = await fetch(API + path);
  const data = await res.json();
  if (!res.ok) throw new Error(data.message || 'Request failed');
  return data;
}

async function post(path, body) {
  const res = await fetch(API + path, {
    method: 'POST', headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(body)
  });
  const data = await res.json();
  if (!res.ok && !data.success) throw new Error(data.message || 'Request failed');
  return data;
}

async function put(path, body) {
  const res = await fetch(API + path, {
    method: 'PUT', headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(body)
  });
  const data = await res.json();
  if (!res.ok) throw new Error(data.message || 'Request failed');
  return data;
}

async function del(path) {
  const res = await fetch(API + path, { method: 'DELETE' });
  const data = await res.json();
  if (!res.ok) throw new Error(data.message || 'Request failed');
  return data;
}

// ==================== UI HELPERS ====================

function val(id) { return document.getElementById(id).value.trim(); }

function showEl(el, msg) {
  el.innerHTML = msg;
  el.classList.remove('hidden');
}

function fmt(n) {
  const num = parseFloat(n) || 0;
  return num.toLocaleString('en-IN', { minimumFractionDigits: 2, maximumFractionDigits: 2 });
}

function tRow(label, value) {
  return `<div class="ticket-row"><span>${label}</span><span>${value}</span></div>`;
}

let toastTimer;
function toast(msg, type = 'info') {
  const el = document.getElementById('toast');
  el.textContent = msg;
  el.className = `toast toast-${type}`;
  clearTimeout(toastTimer);
  toastTimer = setTimeout(() => { el.classList.add('hidden'); }, 4000);
}

// Enter key binding for login
document.addEventListener('DOMContentLoaded', () => {
  const passField = document.getElementById('loginPassword');
  const userField = document.getElementById('loginUsername');
  const handleKey = (e) => { if (e.key === 'Enter') handleLogin(); };
  if (passField) passField.addEventListener('keydown', handleKey);
  if (userField) userField.addEventListener('keydown', handleKey);
});
