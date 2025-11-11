// js/home.js

// Check auth state and load user's name
auth.onAuthStateChanged(async (user) => {
    if (user) {
      try {
        const doc = await db.collection("users").doc(user.uid).get();
        const data = doc.data();
        document.getElementById("welcomeMsg").textContent = `Welcome, ${data.name}!`;
      } catch (error) {
        console.error("Error getting user data:", error);
      }
    } else {
      // Not logged in
      window.location.href = "index.html";
    }
  });
  
  // Handle logout
  document.getElementById("logoutBtn").addEventListener("click", () => {
    auth.signOut()
      .then(() => {
        window.location.href = "index.html";
      })
      .catch((error) => {
        console.error("Logout error:", error);
      });
  });
  