// js/firebase.js

const firebaseConfig = {
  apiKey: "AIzaSyD_mQ4Ext2ma5p4wz0rjz4ccMBIzE7rPiw",
  authDomain: "vic-power.firebaseapp.com",
  projectId: "vic-power",
  storageBucket: "vic-power.appspot.com",
  messagingSenderId: "952244053756",
  appId: "1:952244053756:web:db933cf09e289a7e2ed979",
  measurementId: "G-7Z5DF51HFY"
};

firebase.initializeApp(firebaseConfig);
const auth = firebase.auth();
const db = firebase.firestore();
const storage = firebase.storage();