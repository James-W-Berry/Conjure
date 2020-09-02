/* eslint-disable promise/no-nesting */
/* eslint-disable promise/always-return */
/* eslint-disable no-loop-func */
"use strict";

const functions = require("firebase-functions");
const admin = require("firebase-admin");
const SpotifyWebApi = require("spotify-web-api-node");
const _ = require("lodash");
const Queue = require("smart-request-balancer");
const { merge } = require("lodash");

const spotifyApi = new SpotifyWebApi();
admin.initializeApp({
  credential: admin.credential.cert(require("./service-account.json")),
});

let followedArtistsUris = {};
let numOfArtists = 0;
let numOfAlbums = 0;
let albums = {};
let userId;
let spotifyToken;

const queue = new Queue({
  rules: {
    spotifyAlbums: {
      rate: 1, // one message
      limit: 1, // per second
      priority: 1,
    },
  },
});

const getAllFollowedArtists = async (nextGroupOfArtists) => {
  if (nextGroupOfArtists) {
    return await spotifyApi
      .getFollowedArtists({ limit: 50, after: nextGroupOfArtists })
      .then(async (followedArtists) => {
        numOfArtists += Object.entries(followedArtists.body.artists.items)
          .length;
        Object.entries(followedArtists.body.artists.items).forEach((artist) => {
          admin
            .firestore()
            .collection("users")
            .doc(userId)
            .collection("unprocessedArtists")
            .doc(artist[1].uri)
            .update(
              { name: artist[1].name, uri: artist[1].uri },
              { merge: true }
            )
            .then(() => {
              console.log(`recorded ${artist[1].name}`);
            })
            .catch((error) => {
              console.log(error);
            });
        });

        const more = followedArtists.body.artists.cursors.after;
        if (more) {
          return await getAllFollowedArtists(more);
        } else {
          throw await followedArtists;
        }
      });
  }
};

const getArtistAlbums = async (unprocessedArtists) => {
  let albums = Object.entries(unprocessedArtists).map(async (artist) => {
    console.log(`getting albums for ${artist[0]}`);

    await queue
      .request(
        (retry) =>
          spotifyApi
            .getArtistAlbums(artist[0].split(":")[2])
            .then((response) => response)
            .catch((error) => {
              console.log(error);
              if (error.statusCode === 429) {
                return retry(300); // retry after 10s
              }
              throw error;
            }),
        artist[0].split(":")[2],
        "spotifyAlbums"
      )
      .then(async (response) => {
        Object.entries(response.body.items).forEach(async (album) => {
          await admin
            .firestore()
            .collection("users")
            .doc(userId)
            .collection("albums")
            .doc(album[1].uri)
            .update(
              {
                uri: album[1].uri,
                image: album[1].images[0].url,
                name: album[1].name,
                artist: album[1].artists[0].name,
              },
              { merge: true }
            )
            .then(async () => {
              console.log(`recorded ${album[1].uri}`);
            })
            .catch((error) => {
              console.log(error);
            });
        });
        return Object.entries(response.body.items).length;
      })
      .then(async (albumCount) => {
        await admin
          .firestore()
          .collection("users")
          .doc(userId)
          .update({
            totalAlbums: admin.firestore.FieldValue.increment(albumCount),
          })
          .then(() => {
            console.log(`added ${albumCount} to total album count `);
          })
          .catch((error) => {
            console.log(error);
          });
      })
      .catch((error) => console.error(error));
  });
  return albums;
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

// retrieve followed artists on Spotify
// typically called on initial setup
exports.getSpotifyFollowedArtists = functions.https.onCall(
  async (data, context) => {
    spotifyToken = data.spotifyToken;
    userId = data.userId;

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
        numOfArtists = Object.entries(followedArtists.body.artists.items)
          .length;

        Object.entries(followedArtists.body.artists.items).forEach((artist) => {
          admin
            .firestore()
            .collection("users")
            .doc(userId)
            .collection("unprocessedArtists")
            .doc(artist[1].uri)
            .update(
              { name: artist[1].name, uri: artist[1].uri },
              { merge: true }
            )
            .then(() => {
              console.log(`recorded ${artist[1].name}`);
            })
            .catch((error) => {
              console.log(error);
            });
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

    await admin
      .firestore()
      .collection("users")
      .doc(userId)
      .set({ totalArtists: numOfArtists }, { merge: true })
      .then(() => {
        console.log(`recorded ${numOfArtists} artists`);
      })
      .catch((error) => {
        console.log(error);
      });

    const linkToImageDatabase = generateImageDatabase();
    return linkToImageDatabase;
  }
);

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
    // .collection("processedArtists")
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
  userId = data.userId;

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

// This function will be called several times to retrieve all followed artist's albums,
// over some amount of time, a user's client app will be able to process the
// full library of albums
exports.getBatchOfAlbums = functions.https.onCall(async (data, context) => {
  spotifyToken = data.spotifyToken;
  userId = data.userId;

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

  let unprocessedArtists = {};
  await admin
    .firestore()
    .collection("users")
    .doc(userId)
    .collection("unprocessedArtists")
    .limit(10)
    .get()
    .then((querySnapshot) => {
      querySnapshot.forEach((artist) => {
        unprocessedArtists[artist.id] = artist.data();
      });
      return "done";
    })
    .catch((error) => {
      console.log(error);
      return {};
    });

  await getArtistAlbums(unprocessedArtists);

  Object.entries(unprocessedArtists).forEach((artist) => {
    admin
      .firestore()
      .collection("users")
      .doc(userId)
      .collection("processedArtists")
      .doc(artist[1].uri)
      .update({ name: artist[1].name, uri: artist[1].uri }, { merge: true })
      .then(() => {
        admin
          .firestore()
          .collection("users")
          .doc(userId)
          .collection("unprocessedArtists")
          .doc(artist[1].uri)
          .delete()
          .then(() => {
            console.log(`successfully processed ${artist[1].name}'s albums`);
          })
          .catch((error) => {
            console.log(error);
          });
      })
      .catch((error) => {
        console.log(error);
      });
  });

  admin
    .firestore()
    .collection("users")
    .doc(userId)
    .collection("library")
    .doc("artists")
    .set({ artists: unprocessedArtists }, { merge: true })
    .then(() => {
      console.log("recorded processed albums to library document");
    })
    .catch((error) => {
      console.log(error);
    });

  let result = { status: "success" };
  return result;
});

function generateImageDatabase() {
  return {
    albums: "albums here", //albums,
    followedArtists: "artists here", //followedArtistsUris,
  };
}
