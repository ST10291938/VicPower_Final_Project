// booking.js
document.getElementById("bookingForm").addEventListener("submit", async (e) => {
    e.preventDefault();
  
    const service = document.getElementById("service").value;
    const date = document.getElementById("date").value;
    const time = document.getElementById("time").value;
    const notes = document.getElementById("notes").value;
  
    const user = firebase.auth().currentUser;
    if (!user) {
      alert("Please log in to make a booking.");
      return;
    }
  
    try {
      await firebase.firestore().collection("bookings").add({
        userId: user.uid,
        service,
        date,
        time,
        notes,
        status: "Pending",
        createdAt: firebase.firestore.FieldValue.serverTimestamp()
      });
      alert("Booking successful!");
      document.getElementById("bookingForm").reset();
    } catch (error) {
      alert("Error booking: " + error.message);
    }
  });
  