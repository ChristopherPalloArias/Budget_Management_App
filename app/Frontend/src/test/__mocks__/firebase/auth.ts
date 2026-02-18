// Mock Firebase Auth
export const getAuth = jest.fn(() => ({
  currentUser: null,
}));

export const signInWithEmailAndPassword = jest.fn();
export const signInWithPopup = jest.fn();
export const signOut = jest.fn();
export const createUserWithEmailAndPassword = jest.fn();
export const updateProfile = jest.fn();
export const onAuthStateChanged = jest.fn((auth, callback) => {
  // Call callback immediately with null user
  callback(null);
  // Return unsubscribe function
  return jest.fn();
});

export const GoogleAuthProvider = jest.fn();
