"use strict";

const functions = require("firebase-functions");
const admin = require("firebase-admin");
const spotify = require("./spotify");

admin.initializeApp();

exports.setupConjure = functions.https.onCall((data, context) => {
  const spotifyToken = data.spotifyToken;

  if (!(typeof spotifyToken === "string") || spotifyToken.length === 0) {
    throw new functions.https.HttpsError(
      "invalid-argument",
      "The function must be called with " +
        'one argument "spotifyToken" containing a valid Spotify token to add.'
    );
  }

  const imageDatabase = spotify.createImageDatabase(spotifyToken);

  return { imageDatabase: imageDatabase, token: spotifyToken };
});
