// REGISTER
const registerForm = document.getElementById("registerForm");
if (registerForm) {
  registerForm.addEventListener("submit", (e) => {
    e.preventDefault();
    const name = document.getElementById("registerName").value.trim();
    const email = document.getElementById("registerEmail").value.trim();
    const password = document.getElementById("registerPassword").value;
    const empCode = document.getElementById("empCode").value.trim();

    const role = (empCode === "EMP123") ? "employee" : "user"; // 🔑 Hardcoded employee code

    auth.createUserWithEmailAndPassword(email, password)
      .then((userCredential) => {
        const uid = userCredential.user.uid;

        // Send email verification
        userCredential.user.sendEmailVerification();

        // Save user data to Firestore
        return db.collection("users").doc(uid).set({
          name: name,
          email: email,
          role: role
        });
      })
      .then(() => {
        alert("Registration successful! Please check your email to verify your account.");
        window.location.href = "index.html";
      })
      .catch((error) => {
        alert("Registration failed: " + error.message);
      });
  });
}

// LOGIN
const loginForm = document.getElementById("loginForm");
if (loginForm) {
  loginForm.addEventListener("submit", (e) => {
    e.preventDefault();
    const email = document.getElementById("loginEmail").value.trim();
    const password = document.getElementById("loginPassword").value;

    auth.signInWithEmailAndPassword(email, password)
      .then(async (userCredential) => {
        const user = userCredential.user;

        if (!user.emailVerified) {
          alert("Please verify your email before logging in.");
          auth.signOut();
          return;
        }

        // Fetch user data from Firestore
        const userDoc = await db.collection("users").doc(user.uid).get();
        const userData = userDoc.data();

        if (!userData) {
          alert("No user data found.");
          return;
        }

        if (userData.role === "employee") {
          alert("Welcome, Employee!");
          window.location.href = "employee-home.html";
        } else {
          alert("Welcome!");
          window.location.href = "home.html";
        }
      })
      .catch((error) => {
        alert("Login failed: " + error.message);
      });
  });
}
