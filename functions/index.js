/* eslint-disable promise/always-return */
"use strict";

const functions = require("firebase-functions");
const admin = require("firebase-admin");
const SpotifyWebApi = require("spotify-web-api-node");

const spotifyApi = new SpotifyWebApi();
admin.initializeApp({
  credential: admin.credential.cert(require("./service-account.json")),
});

let followedArtistsUris = {};
let testFollowedArtistsUris = {
  "1349": "spotify:artist:150xbiJGnTy1tSBZ9EFKnT",
  "1914": "spotify:artist:0IpKWttPlwhd7Cevxsh1Bz",
  Belzebubs: "spotify:artist:025JPk5p3gZe5mDNhlCL8V",
  Krypts: "spotify:artist:03p1MQHjSpOnF92Q7OCy9d",
  Esoctrilihum: "spotify:artist:09D139kQVvZ6lw03l4wNI7",
  Dismember: "spotify:artist:09OwM7iXyoFAOzQUlWeDgB",
  Primordial: "spotify:artist:0BZr6WHaejNA63uhZZZZek",
  "Big K.R.I.T.": "spotify:artist:0CKa42Jqrc9fSFbDjePaXP",
  Akitsa: "spotify:artist:0CTKq7arHzG8F0ToHBW8qj",
  LIK: "spotify:artist:0D5ly26kWmFNj6cbzIpJn7",
  IQ: "spotify:artist:0DQ9JcIWMGVzlQSfCVK4oN",
  Gojira: "spotify:artist:0GDGKpJFhVpcjIGF8N6Ewt",
  Wardruna: "spotify:artist:0NJ6wlOAsAJ1PN4VRdTPKA",
  Convulsing: "spotify:artist:0PAGKB3V8JdsjEe84hiITM",
  Gorguts: "spotify:artist:0Q4ioaeWOOlKFVPNdFvp9q",
  Inexorum: "spotify:artist:0SAyfkoQF4Z2pZtB7pon2c",
  "Sempiternal Dusk": "spotify:artist:0SgevvJY7QCSMdNPixtyPF",
  Rattenfänger: "spotify:artist:0Tgtl5beIMahbtIzV5jBXw",
  "Void Meditation Cult": "spotify:artist:0UsxjTGzgdR88GSwOiRmuC",
  Kvelertak: "spotify:artist:0VE0GTaTSeeGSzrQpLmeb9",
  Ride: "spotify:artist:0WPY9nnBy01s5QOt4o4oQX",
  Pissgrave: "spotify:artist:0XFSKdTpzCOOfTPwIdByqf",
  "Sorcier des Glaces": "spotify:artist:0Xtu1ZWY9kGDvMSpVJT0dQ",
  Possessed: "spotify:artist:0ZEpcKtaM4ItvzHJCg5udc",
  "Michael Kiwanuka": "spotify:artist:0bzfPKdbXL5ezYW2z3UGQj",
  "Blut Aus Nord": "spotify:artist:0c0xIXQhCbmtvzM93liaSf",
  Winterfylleth: "spotify:artist:0cKyWvYnOGpPV2NpulEYf5",
  Alcest: "spotify:artist:0d5ZwMtCer8dQdOPAgWhe7",
  Mayhem: "spotify:artist:0dR10i73opHXuRuLbgxltM",
  Gatecreeper: "spotify:artist:0eCB2pwtPnLywA4rxe4i4N",
  "King Dude": "spotify:artist:0erhjm5MwzWpFRFuorXLT2",
  "Temple of Void": "spotify:artist:0gCLnqVApwj3H6EqfaZZUi",
  "Paradise Lost": "spotify:artist:0gIo6kGl4KsCeIbqtZVHYp",
  "Dark Fortress": "spotify:artist:0hiccXRDVXl9sOZ4R7aNry",
  Winterwolf: "spotify:artist:0i8pqigDVvVXfmrG9rEBp7",
  Bergrizen: "spotify:artist:0jZOqsOwLyN5UBRYr5rRpq",
  Eldamar: "spotify:artist:0mZRMbP7xU91E1FGvKbxhF",
  Sanguisugabogg: "spotify:artist:0n98EIfTSiyxUZHUojHykN",
  Xasthur: "spotify:artist:0nH9qzKJMfrJNnF37nDaVj",
  Auðn: "spotify:artist:0qT1hWvLtvRxUqbCmeb7GP",
  "Sacred Son": "spotify:artist:0s97bcKud0kSTYOwTxASNk",
  "Carpathian Forest": "spotify:artist:0sFntmE9T1tiF6G6O6Lm3H",
  Shylmagoghnar: "spotify:artist:0sfWl1dWLgEtMy9oFnNoDA",
  Malokarpatan: "spotify:artist:0ubZb5LkGMpWUps33ulYrl",
  Witchery: "spotify:artist:0ucuxyPTAHv0TPkA1WBV10",
  "Crow Black Sky": "spotify:artist:0ufGeNDtIpcYYQxI3JyW9N",
  "Swallow The Sun": "spotify:artist:0ulKlNlo0iPx5ZS6VMTHWQ",
  Asagraum: "spotify:artist:0vYB7kRlczfzsOnK49N7D2",
  Borknagar: "spotify:artist:0vxxsjcDy61x5zvrOqwHIL",
  Lvcifyre: "spotify:artist:0wNYSaUZ0rbXeHzuPeSY8V",
  Adversarial: "spotify:artist:0wO2rXxz6XE5FJE7bcVRu6",
  ColdWorld: "spotify:artist:0y9T7O1q6PyYt8fOSJfRm5",
  Opeth: "spotify:artist:0ybFZ2Ab08V8hueghSXm6E",
  Nihilist: "spotify:artist:11CFp8UNM1BN6f37Y8b2Z1",
  Wolfheart: "spotify:artist:11EAP8MX0cfrW9lzkK424H",
  Conan: "spotify:artist:11tiA94VlUZOUTVCdQYw3a",
  Hate: "spotify:artist:13XggX75z0Hk0VNvOuTJoB",
  "Lake Of Tears": "spotify:artist:13ltbymjg9upz4wOoF5TTs",
  "A$AP Rocky": "spotify:artist:13ubrt8QOOCPljQ2FL1Kca",
  Batushka: "spotify:artist:15LsRgSmN0t8VLcsUFYW5J",
  "Uncle Acid & The Deadbeats": "spotify:artist:16PcI6JjJuUfPlsX8Ffvfl",
  Decapitated: "spotify:artist:17MbhJOaaPHuWnRaWU9xkc",
  Vindland: "spotify:artist:19HwR4sbIrwyleqp25kmiJ",
  "40 Watt Sun": "spotify:artist:19pyT3VlqEXiozOlxsk1vt",
  Cosmovore: "spotify:artist:1BUmeLZ8suT7EpTD4ntmdn",
  Vulture: "spotify:artist:1CT8OXcMKm9zlFcSX74tFF",
  Nyctalgia: "spotify:artist:1Cnhl9oVtvD69sIe3J55jJ",
  Forndom: "spotify:artist:1DO3ytbfXlJUOoy77yH7IH",
  Selbst: "spotify:artist:1EiQbUfIU3UhBZR5sQRTHN",
  Abyssal: "spotify:artist:1Ey4DaBUTa8vzDU25Go0w2",
  "Cult Of Fire": "spotify:artist:1GNzgaXdYGtEP5xOqCsdSq",
  "AU Dessus": "spotify:artist:1I0Ym6WUjd1lpJZaKNZ6oa",
  Slayer: "spotify:artist:1IQ2e1buppatiN1bxUVkrk",
  "Negative Voice": "spotify:artist:1JLfE5D1uUNxaFI2r2QXkr",
  Ploughshare: "spotify:artist:1KGBv3G6jQPywLMJ1vwsYW",
  Turia: "spotify:artist:1M4G9eIvgvQOmx36Sxtbtp",
  Behemoth: "spotify:artist:1MK0sGeyTNkbefYGj673e9",
  Horrendous: "spotify:artist:1Na6P4MqOassrZtvT9Z12s",
  "The Ruins Of Beverast": "spotify:artist:1RtWv2DQzIGsnjHPXBRaR5",
  "Bill Withers": "spotify:artist:1ThoqLcyIYvZn7iWbj8fsj",
  "The Great Old Ones": "spotify:artist:1U6VZQrgQ4CNXbS6FpP0QC",
  Vastum: "spotify:artist:1UKozzDpsuCLjwE7u5Xjdq",
  "Idle Hands": "spotify:artist:1W5khHrkcznsyChxwIBfAI",
  Portal: "spotify:artist:1WiZz4d759EPtACSUBEfmt",
  "Obtained Enslavement": "spotify:artist:1WivcWZchSUgoMru4LP3h9",
  "The Weeknd": "spotify:artist:1Xyo4u8uXC1ZmMpatF05PJ",
  "Druadan Forest": "spotify:artist:1YsW5TyyqPAomPet0olxoo",
  Unreqvited: "spotify:artist:1ZYRTJCj869ya07OpEAlGR",
  "Caustic Wound": "spotify:artist:1e09BMPCOwTzriWkruRlbi",
  Ritualization: "spotify:artist:1eARrKx9ZkRxLCkU8o3ilo",
  Abbath: "spotify:artist:1epGwdbjU7JSGVBHlqptpx",
  Haunter: "spotify:artist:1f3uDHQIT4CVyqHLe3jwa2",
  Undergang: "spotify:artist:1fFtjgqU6Z2vKBmk5Z8z3o",
  Misotheist: "spotify:artist:1iEMVZeKXEvToyu9iZsRIB",
  Ellende: "spotify:artist:1iLdVM2KFAHUbpaC5wpMbO",
  Aorlhac: "spotify:artist:1jJSxBoAuvuTFjpCxmVSZc",
  Helrunar: "spotify:artist:1jfFNEqIoGOvZZNaYN9BTV",
  Walknut: "spotify:artist:1jkfwRebDjHPcVpJo9D2ea",
  Zhrine: "spotify:artist:1pbBYjEha7Imflp991yDzG",
  Malist: "spotify:artist:1sHOFQsNYODFf01vwCnTww",
  Fetid: "spotify:artist:1shPzjNkV2NdVUYsLEoeHS",
  Altarage: "spotify:artist:1t6R1cmictIjcNCPKO4Nsp",
  Jubal: "spotify:artist:1uA9SJg2c7vVfyLWMSbShK",
  "Witch Vomit": "spotify:artist:1uKzVnmzBtqIzGzjBojwkL",
  Graceless: "spotify:artist:1uevFXuxN8JN4HDnBfcTTh",
  Gravestone: "spotify:artist:1urVyLjfYpkhcORUXUgy0p",
  Undeath: "spotify:artist:1vbr39xw4sKFUOiogA0DWN",
  Kawir: "spotify:artist:1wBSwxYOA4GFZcemohgd5z",
  "Children Of Bodom": "spotify:artist:1xUhNgw4eJDZfvumIpcz1B",
  "Tomb Mold": "spotify:artist:1zFGR4NaBbCDBGwifPKGfM",
  Satyricon: "spotify:artist:221Rd0FvVxMx7eCbWqjiKd",
  Vargrav: "spotify:artist:22zsDWxM8e5bDZqD9J4Vw1",
  Vacivus: "spotify:artist:262YmXQ7B0sF4OzScs6fLw",
  Dawn: "spotify:artist:26QI3eOxUdcWfTwEGJyslB",
  Midnight: "spotify:artist:28KjD5HkkDDytQzKW7JzTp",
  Grave: "spotify:artist:28imYdYhi5ieRXvgYwiIdi",
  Behexen: "spotify:artist:2A56suoU3YFYLIp7wHde6c",
  Ataraxy: "spotify:artist:2A6XErOitAdWyf38RSXYbI",
  Katatonia: "spotify:artist:2CWWgbxApjbyByxBBCvGTm",
  Taake: "spotify:artist:2CofVEvqc6hhyKwYsLiuN9",
  Ihsahn: "spotify:artist:2E1jLcUfqd9w2XtybNB2Za",
  Nightbringer: "spotify:artist:2HFTnH095WzPowwgGP7pFo",
  "Shawn James": "spotify:artist:2HPYUQ6GsPbZHvkyYe2jdm",
  Enslaved: "spotify:artist:2HmtB6wVRRi3z0JwZHtkiD",
  "Desolate Shrine": "spotify:artist:2J6zxkhrtVDq8mDSPiObX2",
  Panopticon: "spotify:artist:2Mz5qpR3WxbcBwZBsmraWE",
  Ungfell: "spotify:artist:2OGO1Fv4QCiCbjXY2gMidQ",
  Weregoat: "spotify:artist:2RnOsxS4tXsYrtnmqtKY0l",
  Sarkrista: "spotify:artist:2VCOvrl9WTbFAkzAs6N2Au",
  Barus: "spotify:artist:2Xp4w9XoOXuwbSTDFr1uhi",
  Doedsvangr: "spotify:artist:2Xu47TsbFIn7GvlEMjGmh0",
  "Kendrick Lamar": "spotify:artist:2YZyLoL8N0Wb9xBt1NhZWg",
  "Downfall Of Gaia": "spotify:artist:2ZBAmlpAv7mVb9ZqUuHLSg",
  Havukruunu: "spotify:artist:2ccokPZoXdXaj4yiZqMxIJ",
  "Sunn 0)))": "spotify:artist:2e7hYqRjL82c1nIoREHc4J",
  Asphyx: "spotify:artist:2eIvR1DzWm5GAufWC2rcGr",
  "Cosmic Church": "spotify:artist:2fVZ5Xn5dtDRibtRNWrHps",
  Afflicted: "spotify:artist:2gyO3HQPAFfnNNFwqeELNY",
  Hammerhedd: "spotify:artist:2hXhcmGIY6NgJL8eQRoA5d",
  "Front Beast": "spotify:artist:2ic71d7SMSDbTLnBkJFX0P",
  Havok: "spotify:artist:2jw4wgixxa20jls9N3Bdpq",
  Marduk: "spotify:artist:2lxB5NTcQXj7GGRR4xAVaH",
  Immortal: "spotify:artist:2mVTkiwfm4ic6DnHpmFq8K",
  "Deceased…": "spotify:artist:2nJopqKVXGa0RHy0t3DypB",
  Entombed: "spotify:artist:2pnezMcaiTHfGmgmGQjLsB",
  Vader: "spotify:artist:2s5DSt9VBNzAn2TbtDHzFZ",
  "Judas Priest": "spotify:artist:2tRsMl4eGxwoNabM08Dm4I",
  Svartidaudi: "spotify:artist:2u9kJMfjnv8FPLtTP3zLWo",
  Cobalt: "spotify:artist:2yawagTvYt4y9mXm0d3n3p",
  Metallica: "spotify:artist:2ye2Wgw4gimLv2eAKyk1NB",
  Pallbearer: "spotify:artist:2yeEmsTQMNHBlS5dhWtuD1",
  Windir: "spotify:artist:2ytfu1MWsf763hCBQmaQr6",
  "Benjamin Tod": "spotify:artist:30hVqCpEQ8gBRdNvgWMr20",
  Azelisassath: "spotify:artist:322iFjSxsqa3muVeByQvcO",
  "Nephilim's Noose": "spotify:artist:32Ih09eElz7Z5PFDeawJy3",
  Misþyrming: "spotify:artist:32zyj5MW8nhKGMaGlp4njt",
  "Under The Church": "spotify:artist:332Ke27zimfLNDbbFNNenx",
  Coffins: "spotify:artist:34Dog4Ylyg3eUFJF5Rk7Hc",
  "Morbid Angel": "spotify:artist:35jmO5o3AhUV70kiR7u4Nw",
  Kampfar: "spotify:artist:35nZyw3d7OIbhGeqBEPIYQ",
  Triptykon: "spotify:artist:37fG1EQuwb6zBHyFYUGfj8",
  "Sivyj Yar": "spotify:artist:38kNVRmj61FK2H460CQ3nX",
  Kreator: "spotify:artist:3BM0EaYmkKWuPmmHFUTQHv",
  Ulcerate: "spotify:artist:3Bv5btxNbwbt79fcjw9DAg",
  "Burial Invocation": "spotify:artist:3DbeYLUZ92Ie6XpNRabsG5",
  Beherit: "spotify:artist:3EHztVB0kFpqp0N8MlmD1X",
  "Black Breath": "spotify:artist:3G5hGmHXhRi8zuIfLAeoPg",
  "Mournful Congregation": "spotify:artist:3HZQm7muBa4Ho0p8K9X82M",
  Highland: "spotify:artist:3IQKOKEP2MMAdZ2V3LUCux",
  Darkane: "spotify:artist:3K43KRzvjn3z36mHpBYSVb",
  Voidceremony: "spotify:artist:3KZGZx0gmgB7hlMrt7sxsB",
  "Chthe'ilist": "spotify:artist:3MFFalA1IZ44q27NwqeCN1",
  Unleashed: "spotify:artist:3MKwCexzAd8YTdsSjRkKbv",
  Agalloch: "spotify:artist:3Meu28o8P5z9Zjm6NTGihT",
  Phobocosm: "spotify:artist:3RNAb5w4D2AnFJeeVqrXFQ",
  Necrophobic: "spotify:artist:3RUZtC4rkONY3kprM10ZTa",
  Malthusian: "spotify:artist:3RpXJfRQjmI3gaTk6GnrZ3",
  "Entombed A.D.": "spotify:artist:3TK5LJPqg2AnBdrp9Xrzmu",
  "Power Trip": "spotify:artist:3TmaQHOfq1olDKHtE38zYT",
  "Rivers of Nihil": "spotify:artist:3UJmyt9ApeZTmOlMvBNGLN",
  Varathron: "spotify:artist:3V9JzR4lFUlNvRgRuPO8x4",
  "Oranssi Pazuzu": "spotify:artist:3XFQRe2FsSOjrODygK4caW",
  Absu: "spotify:artist:3eVkhnD2UKVjX5uiUsqfXP",
  "Emyn Muil": "spotify:artist:3eX7QMrVIouWVnZjWkMq8u",
  Meshuggah: "spotify:artist:3ggwAqZD3lyT2sbovlmfQY",
  Failure: "spotify:artist:3grvcGPaLhfrD5CYsecr4j",
  Archgoat: "spotify:artist:3n2bgWmlyHgwnd9aPmnTM4",
  "Irkallian Oracle": "spotify:artist:3nXt3HeznADl7WeOirrbRv",
  Mitochondrion: "spotify:artist:3ng7UwBECtnIlNIdufUagO",
  "Macabre Omen": "spotify:artist:3oV8g2PkoJsyxdVChVyoPt",
  "Amon Amarth": "spotify:artist:3pulcT2wt7FEG10lQlqDJL",
  Krisiun: "spotify:artist:3pzAW9xs16rX8fukH8wV6N",
  Jakey: "spotify:artist:3q1NKu1dVzFcBfxFos4kE3",
  Hexvessel: "spotify:artist:3rLgIB7dHh2MGYpiOMajJI",
  "Midnight Odyssey": "spotify:artist:3rTyM1AkQ1ymEyP3DxhiqL",
  Immolation: "spotify:artist:3rt16vhD1OuULlsyxUUWIt",
  Sarke: "spotify:artist:3sJXLmH6J5IDiJpxJt8NSN",
  "Legion Of The Damned": "spotify:artist:3tS2n2PiBzG8Mr8nCfLIJy",
  Insomnium: "spotify:artist:3uIgLG971oRM5fe6v8lvQS",
  "Diabolic Night": "spotify:artist:3wnTCtUXbClNxJNfYtH0mF",
  MĘKA: "spotify:artist:41K3KkjuNXQxc94iYrFVpT",
  Almyrkvi: "spotify:artist:41ff5azhY17WAtdTkudNmD",
  "Funereal Presence": "spotify:artist:41gsLwzRav9guke6qG0k8l",
  Extremity: "spotify:artist:42iMHtuQke7Z1refGmTW3w",
  Cloak: "spotify:artist:44mrDjAyvYUG7wJZB2udrT",
  "Torture Rack": "spotify:artist:48pICddH6n80SxXavBasPx",
  "Ripped to Shreds": "spotify:artist:4A5KPL22zRCWO0ysGjYbD7",
  "Funeral Leech": "spotify:artist:4BO9lOcFn9T1sRQlssrd0h",
  Inquisition: "spotify:artist:4BXKhsgc3Bx8Etj7A128Vm",
  Sielunvihollinen: "spotify:artist:4Bwb7ZVphhjbdju2wFYj8G",
  Cytotoxin: "spotify:artist:4CIwd3LXEtN3xLe7PwWMNF",
  Wayfarer: "spotify:artist:4HcBIH7pVbrRRwHnEqxpka",
  БАТЮШКА: "spotify:artist:4JbGNRHZzUZLWOWMTIXRdw",
  "Thou Art Lord": "spotify:artist:4Je2gpohgq9uukyvm9NFm4",
  Thou: "spotify:artist:4KoZpKiPeX4jIi7Euwcfuo",
  "High Command": "spotify:artist:4M3EzJBgOCW0MFc54EBEqQ",
  Sleep: "spotify:artist:4Mt6w4tDGiPgV5q6JWPlrI",
  "Malignant Altar": "spotify:artist:4OWgwvzFQaaOb7lMahkNRy",
  Watain: "spotify:artist:4OpHsZuhfJMU9PZ3zkyUQX",
  Moonsorrow: "spotify:artist:4PdaU6ArZ8JTZvCX9ZWuTI",
  Torche: "spotify:artist:4PxqJghOAEvatt0scJvili",
  "Triumvir Foul": "spotify:artist:4Qjvt5uzjF8TA4sNS58xe6",
  "Skeletal Remains": "spotify:artist:4Rh3NPxVMADeaSIduAsykN",
  "Hate Eternal": "spotify:artist:4Ru4H8nvpjbqHFStsJJcHV",
  Havohej: "spotify:artist:4Svv557NlcxW0Kykb54MRP",
  "Hideous Divinity": "spotify:artist:4XOpU6j3NQ37JfIaWA4ySH",
  "Falls of Rauros": "spotify:artist:4Y5CKbAT8fHWEbpLGFNygB",
  "Void Omnia": "spotify:artist:4YCM6VIbVM2VOogQlYNhOJ",
  "Celtic Frost": "spotify:artist:4ZISAmHmQUDCpv8xydqeKG",
  Carnage: "spotify:artist:4aFjkvXJvZCBDIbHESP4qi",
  Sortilegia: "spotify:artist:4brRDztU4WNs8O8K8bsShe",
  "Secrets Of The Moon": "spotify:artist:4cLxgMgH07mY1zQOlEHykC",
  Cruciamentum: "spotify:artist:4cM4Vlx41zMs5lLQcSFlTy",
  Death: "spotify:artist:4f5V3PQ66nIrBCqugJtaGn",
  "Patrons of the Rotting Gate": "spotify:artist:4jkGQxfcs5YM5DtxjcLxa8",
  Mutiilation: "spotify:artist:4lbKjEvAKni4q1GLsDcvZI",
  "Deathspell Omega": "spotify:artist:4pP3Gtdmwp2cHtz736pyGI",
  Drudkh: "spotify:artist:4q5mj9YpaYesKvHzN8XYve",
  Saor: "spotify:artist:4rHMzJ1RKUMtid1K2QEYbr",
  Urgehal: "spotify:artist:4rsH5QGFETyQgsmU17kbVS",
  "Yellow Eyes": "spotify:artist:4sriy0rUaDm6U7cNo5FeSK",
  "Human Serpent": "spotify:artist:4tz5JiOjEBkyLvGSeEjmTy",
  Twilight: "spotify:artist:4ueuXzGilot65euwJYz0zm",
  "The Allman Brothers Band": "spotify:artist:4wQ3PyMz3WwJGI5uEqHUVR",
  "The Black Dahlia Murder": "spotify:artist:4xTDPgk4jHCF0qui3dH6BS",
  "Imperium Dekadenz": "spotify:artist:4ykmWeGRRY1OmPgMY4K2rw",
  Revenge: "spotify:artist:50U1jrMV6FoddCGhjovDpG",
  Nocturnus: "spotify:artist:5ATaBoi3yPQE7k3PD7TuDB",
  "Dawn of Disease": "spotify:artist:5BHxUgDTEFplxKa00vDWfO",
  "Whoredom Rife": "spotify:artist:5BPuwHD7jA85XePrEFUIIk",
  Summoning: "spotify:artist:5BViLZRXrRfhPDokkbYiMy",
  Khemmis: "spotify:artist:5Dejhd4zYKEUm6q1FLr1ik",
  "Judas Iscariot": "spotify:artist:5G1MlYyOsiG56mjlsZHsLC",
  "Spectral Voice": "spotify:artist:5I4bpzP0gZSVwceopnaoVb",
  Equilibrium: "spotify:artist:5KvkOKroKLz202ioXfGWR2",
  Obsequiae: "spotify:artist:5KwdkxxLA3mR8dxpzauigT",
  "Porcupine Tree": "spotify:artist:5NXHXK6hOCotCF8lvGM1I0",
  "Diabolical Masquerade": "spotify:artist:5NiGzteFr6N4yfGV2nvTsf",
  "Dead Congregation": "spotify:artist:5PBErxkQsE0lzNQPrn5oK3",
  "Wretched Fate": "spotify:artist:5RXIMsPlvRJaQIMMivH8m7",
  Sinmara: "spotify:artist:5SLDzWl9xD7bqN154mLi0Y",
  "The Wakedead Gathering": "spotify:artist:5ULRJ7TzdpQVv58cahjZsZ",
  Entrails: "spotify:artist:5WOBHV40DFyglQzLVjoH2s",
  "Impetuous Ritual": "spotify:artist:5WpW0XFUSrMfj4zlLqCige",
  Helheim: "spotify:artist:5XbRdXn5rKF7kVjGaqsShZ",
  Aevangelist: "spotify:artist:5ZTig9T9bWHXmaTIwgdh2h",
  Phrenelith: "spotify:artist:5b57JHCmIS84OjcP71WFtw",
  Nagelfar: "spotify:artist:5bfzXMJRSxd7egCqTI8UHo",
  "Ascended Dead": "spotify:artist:5eAE0zR4YcmlcfyNeAVyEP",
  Wormwitch: "spotify:artist:5eTTS3YDf54li8yguyoP14",
  "Violet Cold": "spotify:artist:5eh1n96NC6g34nPqpIItIo",
  Venom: "spotify:artist:5fwaejlOHVBAw1KhIPPaQe",
  "Fever Ray": "spotify:artist:5hE6NCoobhyEu6TRSbjOJY",
  Necrot: "spotify:artist:5jWIRPU5rVBm6ky8Atq7AW",
  "Lunatic Soul": "spotify:artist:5kL0FLCWlo2xLmyRI68bW4",
  Carcass: "spotify:artist:5lhaM01nwvsMZpmPY2HVER",
  "Wolves In The Throne Room": "spotify:artist:5lqyPWmAivV75tII5Vxpet",
  Dynfari: "spotify:artist:5lx20Bm2f3a5FTTy1cS1D9",
  Deathrite: "spotify:artist:5rtmrK3GOpfRohywB44MPV",
  "Spirit Adrift": "spotify:artist:5sW5eR9g4kNibasfrlw4EN",
  "Warp Chamber": "spotify:artist:5tG11cjFU7s5yuRA8z7yvf",
  Evilfeast: "spotify:artist:5wnDSNOpVzG3iMuBd2kvjK",
  "Nokturnal Mortum": "spotify:artist:5xXpxy1ik670YwTolmT7Yr",
  "Infernal Coil": "spotify:artist:5yjzuDAXRgVE2PPo0tc6Sy",
  "Imperial Triumphant": "spotify:artist:5zvMklMSTgoGUS9Un5domO",
  Bethlehem: "spotify:artist:60hOd90CQglmxfBYSO7dqV",
  "Lost Soul": "spotify:artist:62XGlCPVw4mRaVGtwzpe1z",
  Incantation: "spotify:artist:66AXpP038gUcoKVQoO98Fz",
  "Black Curse": "spotify:artist:67L4Rj0u4RzUC1bTMW53xu",
  "Cattle Decapitation": "spotify:artist:67ZMMtA88DDO0gTuRrzGjn",
  Horna: "spotify:artist:69Sr8wyHst8byPxRXENs3D",
  Tithe: "spotify:artist:6BXoHovZZRZ8AbUyZmq3Kw",
  Leviathan: "spotify:artist:6CJU2RwwB10D0KoYYqWYeM",
  Urfaust: "spotify:artist:6D3oL9YvZBgd3IVFWUJ2BI",
  "Blood Incantation": "spotify:artist:6FGv87WQ3mJWn3cmLUww6x",
  Ováte: "spotify:artist:6GoZo80jsv8AIMB4flJJ1C",
  Slaegt: "spotify:artist:6IisDVoZ1J7CpAc8AF4aU3",
  Kriegsmaschine: "spotify:artist:6NPTNhxwqCO1vFQ2Zpuut7",
  "An Isolated Mind": "spotify:artist:6O8aGlR5LBwVWwEFzDpiOI",
  Profanatica: "spotify:artist:6OGNyv8Z63eYqmkN17mpwW",
  Swallowed: "spotify:artist:6S8B6l2XY7ZbprjtHVB5XC",
  Sodom: "spotify:artist:6SYbLA9utoNsllunR1TnkM",
  "Satanic Warmaster": "spotify:artist:6SnNN1sTSAZaHfz5Gdl3PX",
  Mefitis: "spotify:artist:6UHkgGj6P7Ty1BOmneYnCa",
  Sühnopfer: "spotify:artist:6XHbuOX6w1yNAjIm8Bc3JY",
  "At The Gates": "spotify:artist:6YXarbjg36ODFPez0PnOlD",
  Ulver: "spotify:artist:6bYFkBNvayh3nGqxcPp7Sv",
  Kalmankantaja: "spotify:artist:6cvfJNOW4ZAMiSVLEbeP8P",
  "Cultes des Ghoules": "spotify:artist:6dWOp6U7Z8HAvhT2y4uomM",
  "Chaos Moon": "spotify:artist:6h5jPO4MseO3g1ZbY0n2rn",
  Morvigor: "spotify:artist:6havBNPBvuwfYDeezBjice",
  "Mare Cognitum": "spotify:artist:6hk43KSfwt4aYNH5N4qKcO",
  Emperor: "spotify:artist:6jPWrSmxOd9mj7Xaj4EP2Y",
  "Bell Witch": "spotify:artist:6lZ0xXnt7D1JXxv03XLX0K",
  Un: "spotify:artist:6mC3VYuPFT1UdH1U1WLDRU",
  Ascension: "spotify:artist:6pshrtl2rozQucs5ve4wYn",
  "Code Orange": "spotify:artist:6qtECqesbU29iftyeWmldK",
  Bathory: "spotify:artist:6rBvjnvdsRxFRSrq1StGOM",
  Ulthar: "spotify:artist:6s1hQzoXYdEjYdJiHbu1iZ",
  "Sun Worship": "spotify:artist:6szTrm1U4z8RaKP3wM90r3",
  Amebix: "spotify:artist:6tUC9fQSG7uqbzUy9AJ1NY",
  Bölzer: "spotify:artist:6toR2I8BssfcGNJWkL2S0W",
  "Devin Townsend": "spotify:artist:6uejjWIOshliv2Ho0OJAQN",
  Wintersun: "spotify:artist:6ui6SwChan7c1KYBQCqGKV",
  Mortiferum: "spotify:artist:6vhs1oAtSyMHsn9en6BaMD",
  Hällas: "spotify:artist:6wL917RC8KC0ZwDbqmO60r",
  "Negative Plane": "spotify:artist:6wLrUGYOX9uKdKxi2crU6b",
  JPEGMAFIA: "spotify:artist:6yJ6QQ3Y5l0s0tn7b0arrO",
  Mortuary: "spotify:artist:6zT6iutCzWCXAuD3P8zkqp",
  Sargeist: "spotify:artist:6zl5IXVgj1nG1fqjUh68xe",
  Grift: "spotify:artist:74Fmoi6WyKpTp6NkjRWfhf",
  Magoth: "spotify:artist:75o9HCexAwU1en1X0wVLU1",
  "Árstíðir lífsins": "spotify:artist:76NNFfoAUYwlfJtcz5yoiP",
  Arckanum: "spotify:artist:7CXgiYtZiPnwxHM8ad4hNZ",
  Thantifaxath: "spotify:artist:7DcSKG8ruWMgHsUh1rqZFh",
  "Rotting Christ": "spotify:artist:7FhkwcO8Jd7BRWdllBpXBJ",
  "Trees of Eternity": "spotify:artist:7IxOJnsT8vXhTTzb6nlPOO",
  Burzum: "spotify:artist:7L6u6TyhjuwubrcojPeNgf",
  "Dawn Of Solace": "spotify:artist:7anjHetVbdSelRpDsZzWY7",
  "Genocide Pact": "spotify:artist:7aqcTzkV6MaDOmeC1anf8O",
  "Paysage D'Hiver": "spotify:artist:7cQYuSfsavW0BrcspRRJMi",
  "Spectral Lore": "spotify:artist:7drtlGb644Gyyl8nxGX65H",
  Bloodbath: "spotify:artist:7eYmDBinb760MUIfoRdlGQ",
  Potentiam: "spotify:artist:7ecgZb9ohE427K9hVbfgoQ",
  Schammasch: "spotify:artist:7fZssjCP2TtFtrEkZOX1t9",
  "Time Lurker": "spotify:artist:7ffE7dg7LlqyOi4plNO7DR",
  "Gaahls WYRD": "spotify:artist:7h8V7XHdJ3akShW3uFaZyn",
  "Roky Erickson": "spotify:artist:7hCsRnXtcbez8msLPfjbkz",
  Scumpulse: "spotify:artist:7jyafuRMRDMuCbIfNfDjDK",
  Darkthrone: "spotify:artist:7kWnE981vITXDnAD2cZmCV",
  Skogen: "spotify:artist:7kpKkXQDtgPNVdghC0mkDf",
  Valkyrja: "spotify:artist:7lscFXsWNk0vWfHwgAHLR1",
  "Sulphur Aeon": "spotify:artist:7nOT3FnmKEzbGvTS2fbeaV",
  "Mammoth Grinder": "spotify:artist:7pIDCJJU7yAb4CIlaj9h3f",
  Slegest: "spotify:artist:7pxCrLh43flDN5VCB4UoJq",
  Qrixkuor: "spotify:artist:7rzT9Xd3AMiu9PVmcNKGCF",
  Carnation: "spotify:artist:7s6SmrbyQvp26jN5EbnU9u",
  Heilung: "spotify:artist:7sTKZr30LqC928DZ5P9mNQ",
  Akhlys: "spotify:artist:7swiuMho0UMNYbZqcO0dho",
  "Fetid Zombie": "spotify:artist:7u6T9DCQjUR8CqI4wWsizj",
  Malum: "spotify:artist:7ufisTbpMLnUX2PdSBIXin",
  Nocternity: "spotify:artist:7uorV3fcOMMMZHqsXPL92d",
  Tribulation: "spotify:artist:7xTo7ipdBZezIoyAkmcRge",
  Lantern: "spotify:artist:7zcNrt97Sc67W01HOaTytv",
};
let albums = {};
let linkToImageDatabase;

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

  const writeResult = await admin
    .firestore()
    .collection("users")
    .doc(userId)
    .set({ followedArtists: testFollowedArtistsUris });
  console.log(writeResult);

  // try {
  //   console.log(
  //     await spotifyApi
  //       .getFollowedArtists({ limit: 50 })
  //       .then(async (followedArtists) => {
  //         Object.entries(followedArtists.body.artists.items).forEach(
  //           (artist) => {
  //             followedArtistsUris[artist[1].name] = artist[1].uri;
  //           }
  //         );

  //         const more = followedArtists.body.artists.cursors.after;

  //         if (more) {
  //           await getAllFollowedArtists(more);
  //         }
  //         return true;
  //       })
  //   );
  // } catch (error) {
  //   console.log(error);
  // }

  // let getAlbums = await Promise.all(
  //   Object.entries(testFollowedArtistsUris).map(async (artist) => {
  //     try {
  //       let artistAlbums = await spotifyApi.getArtistAlbums(
  //       artist[1].split(":")[2]
  //     );
  //     Object.entries(artistAlbums.body.items).forEach((album) => {
  //       albums[album[1].id] = album[1].images[0];
  //     });
  //     return true;
  //   } catch (error) {
  //     console.log(error);
  //   }

  //   })
  // );

  // console.log(testFollowedArtistsUris.Abyssal.split(":")[2]);

  // let getAlbums = await spotifyApi.getArtistAlbums(
  //   testFollowedArtistsUris.Abyssal.split(":")[2]
  // );

  // Object.entries(getAlbums.body.items).forEach((album) => {
  //   albums[album[1].id] = album[1].images[0].url;
  // });

  console.log(albums);

  const linkToImages = storeAlbumImages();
  validateAlbumImages(linkToImages);
  linkToImageDatabase = generateImageDatabase();

  return linkToImageDatabase;
});

function storeAlbumImages() {
  return "link to image files here";
}

function validateAlbumImages() {}

function generateImageDatabase() {
  return {
    link: "download link to database here",
    artists: followedArtistsUris,
    albums: albums,
  };
}
