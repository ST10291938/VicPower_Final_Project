// Helper functions for localStorage handling
function getUsers() {
  return JSON.parse(localStorage.getItem("users") || "[]");
}

function setUsers(users) {
  localStorage.setItem("users", JSON.stringify(users));
}

function getEmployees() {
  return JSON.parse(localStorage.getItem("employees") || "[]");
}

function setEmployees(employees) {
  localStorage.setItem("employees", JSON.stringify(employees));
}

function setCurrentUser(user) {
  localStorage.setItem("currentUser", JSON.stringify(user));
}

function getCurrentUser() {
  return JSON.parse(localStorage.getItem("currentUser"));
}

function logoutUser() {
  localStorage.removeItem("currentUser");
  window.location.href = "index.html";  // Redirect to login page on logout
}

function getBookings() {
  return JSON.parse(localStorage.getItem("bookings") || "[]");
}

function saveBookings(bookings) {
  localStorage.setItem("bookings", JSON.stringify(bookings));
}

// Initialize default admin if none exists
if (!localStorage.getItem("employees")) {
  setEmployees([{ name: "Admin", email: "admin@vicpower.com", password: "admin123" }]);
}

// Registration (register.html)
const registerForm = document.getElementById("registerForm");
if (registerForm) {
  registerForm.addEventListener("submit", (e) => {
    e.preventDefault();

    const name = document.getElementById("registerName").value.trim();
    const email = document.getElementById("registerEmail").value.trim();
    const password = document.getElementById("registerPassword").value;

    const users = getUsers();
    const existingUser = users.find(u => u.email === email);

    if (existingUser) {
      alert("An account with this email already exists.");
    } else {
      users.push({ name, email, password });
      setUsers(users);
      alert("Registration successful! You can now log in.");
      window.location.href = "index.html";
    }
  });
}

// Login (index.html)
const loginForm = document.getElementById("loginForm");
if (loginForm) {
  loginForm.addEventListener("submit", (e) => {
    e.preventDefault();

    const email = document.getElementById("loginEmail").value.trim();
    const password = document.getElementById("loginPassword").value;

    const users = getUsers();
    const employees = getEmployees();
    const user = users.find(u => u.email === email && u.password === password);
    const employee = employees.find(e => e.email === email && e.password === password);

    if (user) {
      setCurrentUser({ ...user, role: "user" });
      window.location.href = "home.html";
    } else if (employee) {
      setCurrentUser({ ...employee, role: "employee" });
      window.location.href = "admin.html";
    } else {
      alert("Invalid email or password.");
    }
  });
}

// Home Page (home.html)
const welcomeMsg = document.getElementById("welcomeMsg");
if (welcomeMsg) {
  const user = getCurrentUser();
  if (user) {
    if (user.role === "employee") {
      window.location.href = "admin.html";
    } else {
      welcomeMsg.textContent = `Welcome, ${user.name}!`;
    }
  } else {
    window.location.href = "index.html";
  }
}

// Logout button
const logoutBtn = document.getElementById("logoutBtn");
if (logoutBtn) {
  logoutBtn.addEventListener("click", () => {
    logoutUser();
  });
}

// Booking (booking.html)
const bookingForm = document.getElementById("bookingForm");
if (bookingForm) {
  bookingForm.addEventListener("submit", (e) => {
    e.preventDefault();

    const service = document.getElementById("service").value;
    const date = document.getElementById("date").value;
    const time = document.getElementById("time").value;

    const user = getCurrentUser();
    if (!user || user.role !== "user") {
      alert("Please log in as a user to book.");
      window.location.href = "index.html";
      return;
    }

    const bookings = getBookings();
    bookings.push({
      name: user.name,
      email: user.email,
      service,
      date,
      time,
    });
    saveBookings(bookings);
    alert("Booking successful!");
    window.location.href = "booking-status.html";
  });
}

// Booking Status (booking-status.html)
const statusContainer = document.getElementById("bookingStatus");
if (statusContainer) {
  const user = getCurrentUser();
  if (!user || user.role !== "user") {
    alert("Please log in as a user to view your bookings.");
    window.location.href = "index.html";
  } else {
    const bookings = getBookings().filter(b => b.email === user.email);
    if (bookings.length === 0) {
      statusContainer.innerHTML = "<p>No bookings found.</p>";
    } else {
      bookings.forEach(b => {
        const div = document.createElement("div");
        div.className = "booking-item";
        div.innerHTML = `
          <h3>${b.service}</h3>
          <p><strong>Date:</strong> ${b.date}</p>
          <p><strong>Time:</strong> ${b.time}</p>
        `;
        statusContainer.appendChild(div);
      });
    }
  }
}

// Admin View All Bookings (admin.html)
const adminBookingContainer = document.getElementById("allBookings");
if (adminBookingContainer) {
  const user = getCurrentUser();
  if (!user || user.role !== "employee") {
    alert("Access denied.");
    window.location.href = "index.html";
  } else {
    const bookings = getBookings();
    if (bookings.length === 0) {
      adminBookingContainer.innerHTML = "<p>No bookings available.</p>";
    } else {
      bookings.forEach(b => {
        const div = document.createElement("div");
        div.className = "booking-item";
        div.innerHTML = `
          <h3>${b.service}</h3>
          <p><strong>Name:</strong> ${b.name}</p>
          <p><strong>Email:</strong> ${b.email}</p>
          <p><strong>Date:</strong> ${b.date}</p>
          <p><strong>Time:</strong> ${b.time}</p>
        `;
        adminBookingContainer.appendChild(div);
      });
    }
  }
}

// Employee login form handler
if (!localStorage.getItem("employees")) {
  setEmployees([{ name: "Admin", email: "admin@vicpower.com", password: "admin123" }]);
}

const employeeLoginForm = document.getElementById("employeeLoginForm");
if (employeeLoginForm) {
  employeeLoginForm.addEventListener("submit", (e) => {
    e.preventDefault();

    const email = document.getElementById("employeeEmail").value.trim();
    const password = document.getElementById("employeePassword").value;

    const employees = getEmployees();
    const employee = employees.find(emp => emp.email === email && emp.password === password);

    if (employee) {
      setCurrentUser({ ...employee, role: "employee" });
      alert("Login successful!");
      window.location.href = "admin-dashboard.html";  // Redirect on successful login
    } else {
      alert("Invalid employee credentials.");
    }
  });
}
// ✅ Step 2: Show bookings on admin-dashboard.html
const adminDashboardContainer = document.getElementById("adminBookings");
if (adminDashboardContainer) {
  const user = getCurrentUser();
  if (!user || user.role !== "employee") {
    alert("Access denied. Employees only.");
    window.location.href = "index.html";
  } else {
    const bookings = getBookings();
    if (bookings.length === 0) {
      adminDashboardContainer.innerHTML = "<p>No bookings found.</p>";
    } else {
      bookings.forEach((b, index) => {
        const div = document.createElement("div");
        div.className = "booking-item";
        div.innerHTML = `
          <h3>Booking ${index + 1}</h3>
          <p><strong>Name:</strong> ${b.name}</p>
          <p><strong>Email:</strong> ${b.email}</p>
          <p><strong>Service:</strong> ${b.service}</p>
          <p><strong>Date:</strong> ${b.date}</p>
          <p><strong>Time:</strong> ${b.time}</p>
          <button class="completeBtn" data-index="${index}">Complete</button>
          <hr />
        `;
        adminDashboardContainer.appendChild(div);
      });

      // ✅ Add event listener for all Complete buttons
      adminDashboardContainer.addEventListener("click", (e) => {
        if (e.target.classList.contains("completeBtn")) {
          const index = e.target.dataset.index;
          let bookings = getBookings();
          if (confirm("Mark this booking as completed? It will be removed.")) {
            bookings.splice(index, 1); // remove from array
            saveBookings(bookings);    // save updated list
            alert("Booking marked as completed.");
            location.reload();         // reload admin view
          }
        }
      });
    }
  }
}
