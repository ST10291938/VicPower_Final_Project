// myBookings.js
firebase.auth().onAuthStateChanged(async (user) => {
    if (user) {
      const bookingsList = document.getElementById("bookingsList");
      bookingsList.innerHTML = "<p>Loading your bookings...</p>";
  
      try {
        const snapshot = await firebase.firestore()
          .collection("bookings")
          .where("userId", "==", user.uid)
          .orderBy("createdAt", "desc")
          .get();
  
        bookingsList.innerHTML = "";
  
        if (snapshot.empty) {
          bookingsList.innerHTML = "<p>No bookings found.</p>";
          return;
        }
  
        snapshot.forEach(doc => {
          const data = doc.data();
          const bookingItem = document.createElement("div");
          bookingItem.classList.add("booking-item");
          bookingItem.innerHTML = `
            <h3>${data.service}</h3>
            <p><strong>Date:</strong> ${data.date}</p>
            <p><strong>Time:</strong> ${data.time}</p>
            <p><strong>Status:</strong> ${data.status}</p>
            <p><strong>Notes:</strong> ${data.notes || "None"}</p>
          `;
          bookingsList.appendChild(bookingItem);
        });
      } catch (error) {
        bookingsList.innerHTML = "<p>Error loading bookings.</p>";
        console.error(error);
      }
    } else {
      window.location.href = "login.html";
    }
  });
  