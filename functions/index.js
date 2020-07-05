/* eslint-disable no-loop-func */
/* eslint-disable promise/always-return */
"use strict";

const functions = require("firebase-functions");
const admin = require("firebase-admin");
const SpotifyWebApi = require("spotify-web-api-node");
const _ = require("lodash");

const spotifyApi = new SpotifyWebApi();
admin.initializeApp({
  credential: admin.credential.cert(require("./service-account.json")),
});

let followedArtistsUris = {};
let albums = {};

const getAllFollowedArtists = async (nextGroupOfArtists) => {
  if (nextGroupOfArtists) {
    return await spotifyApi
      .getFollowedArtists({ limit: 50, after: nextGroupOfArtists })
      .then(async (followedArtists) => {
        Object.entries(followedArtists.body.artists.items).forEach((artist) => {
          followedArtistsUris[artist[1].name] = artist[1].uri;
        });

        const more = followedArtists.body.artists.cursors.after;
        if (more) {
          return await getAllFollowedArtists(more);
        } else {
          throw await followedArtists;
        }
      });
  }
  return followedArtistsUris;
};

const getArtistAlbums = async () => {
  Object.entries(followedArtistsUris).map(async ([key, value], index) => {
    if (index < 5) {
      console.log(`getting albums for ${key}`);

      console.log(
        await spotifyApi
          .getArtistAlbums(value.split(":")[2])
          .then((artistAlbums) => {
            Object.entries(artistAlbums.body.items).forEach((album) => {
              albums[album[1].id] = {
                uri: album[1].uri,
                image: album[1].images[0].url,
                name: album[1].name,
                artist: album[1].artists[0].name,
              };
            });
          })
          .catch((error) => {
            console.log(error);
          })
      );
    }
  });
};

function difference(object, base) {
  function changes(object, base) {
    return _.transform(object, (result, value, key) => {
      if (!_.isEqual(value, base[key])) {
        result[key] =
          _.isObject(value) && _.isObject(base[key])
            ? changes(value, base[key])
            : value;
      }
    });
  }
  return changes(object, base);
}

exports.setupConjure = functions.https.onCall(async (data, context) => {
  const spotifyToken = data.spotifyToken;
  const userId = data.userId;

  try {
    if (!(typeof spotifyToken === "string") || spotifyToken.length === 0) {
      throw new functions.https.HttpsError(
        "invalid-argument",
        "The function must be called with " +
          'an argument "spotifyToken" containing a valid Spotify token to add.'
      );
    }

    if (!(typeof userId === "string") || userId.length === 0) {
      throw new functions.https.HttpsError(
        "invalid-argument",
        "The function must be called with " +
          'an argument "userId" containing a valid Spotify user id.'
      );
    }

    spotifyApi.setAccessToken(spotifyToken);
  } catch (error) {
    console.log(error);
  }

  await spotifyApi
    .getFollowedArtists({ limit: 50 })
    .then(async (followedArtists) => {
      Object.entries(followedArtists.body.artists.items).forEach((artist) => {
        followedArtistsUris[artist[1].name] = artist[1].uri;
      });

      const more = followedArtists.body.artists.cursors.after;

      if (more) {
        await getAllFollowedArtists(more);
      }
      return true;
    })
    .catch((error) => {
      console.log(error);
    });

  console.log(
    `retrieved ${Object.keys(followedArtistsUris).length} followed artists`
  );

  await admin
    .firestore()
    .collection("users")
    .doc(userId)
    .set({ followedArtists: followedArtistsUris }, { merge: true })
    .then(() => {
      console.log(
        `recorded ${Object.keys(followedArtistsUris).length} followed artists`
      );
    })
    .catch((error) => {
      console.log(error);
    });

  await getArtistAlbums().then(
    console.log(`retrieved ${Object.keys(albums).length} albums`)
  );

  await admin
    .firestore()
    .collection("users")
    .doc(userId)
    .set({ albums: albums }, { merge: true })
    .then(() => {
      console.log(`recorded ${Object.keys(albums).length} albums`);
    })
    .catch((error) => {
      console.log(error);
    });

  await admin
    .firestore()
    .collection("users")
    .doc(userId)
    .set(
      { totalArtists: Object.keys(followedArtistsUris).length.toString() },
      { merge: true }
    )
    .then(() => {
      console.log(`recorded total artists`);
    })
    .catch((error) => {
      console.log(error);
    });

  await admin
    .firestore()
    .collection("users")
    .doc(userId)
    .set(
      { totalAlbums: Object.keys(albums).length.toString() },
      { merge: true }
    )
    .then(() => {
      console.log(`recorded total albums`);
    })
    .catch((error) => {
      console.log(error);
    });

  const linkToImageDatabase = generateImageDatabase();
  return linkToImageDatabase;
});

// get list of followed artists from Firebase,
// get currently followed artists list from Spotify,
// return diff, in any
exports.checkFollowedArtists = functions.https.onCall(async (data, context) => {
  const spotifyToken = data.spotifyToken;
  const userId = data.userId;

  try {
    if (!(typeof spotifyToken === "string") || spotifyToken.length === 0) {
      throw new functions.https.HttpsError(
        "invalid-argument",
        "The function must be called with " +
          'an argument "spotifyToken" containing a valid Spotify token to add.'
      );
    }

    if (!(typeof userId === "string") || userId.length === 0) {
      throw new functions.https.HttpsError(
        "invalid-argument",
        "The function must be called with " +
          'an argument "userId" containing a valid Spotify user id.'
      );
    }

    spotifyApi.setAccessToken(spotifyToken);
  } catch (error) {
    console.log(error);
  }

  // Spotify followed artists
  await spotifyApi
    .getFollowedArtists({ limit: 50 })
    .then(async (followedArtists) => {
      Object.entries(followedArtists.body.artists.items).forEach((artist) => {
        followedArtistsUris[artist[1].name] = artist[1].uri;
      });

      const more = followedArtists.body.artists.cursors.after;

      if (more) {
        await getAllFollowedArtists(more);
      }
      return true;
    })
    .catch((error) => {
      console.log(error);
    });

  console.log(
    `retrieved ${
      Object.keys(followedArtistsUris).length
    } followed artists from Spotify:`
  );

  Object.entries(followedArtistsUris).map(async ([key, value], index) => {
    if (index < 3) {
      console.log(`${key}: ${value}`);
    }
  });

  // Firebase followed artists
  let firebaseArtists = await admin
    .firestore()
    .collection("users")
    .doc(userId)
    .get()
    .then((doc) => {
      return doc.data().followedArtists;
    })
    .catch((error) => {
      console.log(error);
      return {};
    });

  console.log(
    `retrieved ${Object.keys(firebaseArtists).length} artists from Firebase`
  );

  Object.entries(firebaseArtists).map(async ([key, value], index) => {
    if (index < 3) {
      console.log(`${key}: ${value}`);
    }
  });

  // Find any difference
  let artistDiff = difference(followedArtistsUris, firebaseArtists);
  console.log(`${Object.entries(artistDiff).length} untracked artists`);
  // eslint-disable-next-line array-callback-return
  Object.entries(artistDiff).map(async ([key, value], index) => {
    if (index < 3) {
      console.log(`${key}: ${value}`);
    }
  });

  return { artistDiff: artistDiff };
});

exports.updateConjure = functions.https.onCall(async (data, context) => {
  const spotifyToken = data.spotifyToken;
  const userId = data.userId;

  try {
    if (!(typeof spotifyToken === "string") || spotifyToken.length === 0) {
      throw new functions.https.HttpsError(
        "invalid-argument",
        "The function must be called with " +
          'an argument "spotifyToken" containing a valid Spotify token to add.'
      );
    }

    if (!(typeof userId === "string") || userId.length === 0) {
      throw new functions.https.HttpsError(
        "invalid-argument",
        "The function must be called with " +
          'an argument "userId" containing a valid Spotify user id.'
      );
    }

    spotifyApi.setAccessToken(spotifyToken);
  } catch (error) {
    console.log(error);
  }

  return { result: "updated Conjure with newly followed artists" };
});

function generateImageDatabase() {
  return {
    albums: "albums here", //albums,
    followedArtists: "artists here", //followedArtistsUris,
  };
}
