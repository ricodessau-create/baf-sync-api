import * as functions from "firebase-functions";
import * as admin from "firebase-admin";

admin.initializeApp();
const db = admin.firestore();

export const biersync = functions.https.onRequest(async (req, res) => {
  res.set("Access-Control-Allow-Origin", "*");
  res.set("Access-Control-Allow-Methods", "POST, OPTIONS");
  res.set("Access-Control-Allow-Headers", "Content-Type");

  if (req.method === "OPTIONS") return res.status(204).send("");

  if (req.method !== "POST")
    return res.status(405).send("Method not allowed");

  try {
    const { token, uuid, name } = req.body || {};

    if (!token || !uuid || !name)
      return res.status(400).json({ success: false, message: "Missing fields" });

    const tokenDoc = await db.collection("sync_tokens").doc(token).get();
    if (!tokenDoc.exists)
      return res.status(400).json({ success: false, message: "Ungültiger Code" });

    const uid = tokenDoc.get("uid");
    if (!uid)
      return res.status(400).json({ success: false, message: "Token ohne Benutzer" });

    const userRef = db.collection("users").doc(uid);
    await userRef.set(
      { minecraftUuid: uuid, minecraftName: name },
      { merge: true }
    );

    await tokenDoc.ref.delete();

    const userSnap = await userRef.get();
    const rank = userSnap.get("rank") || "malzbier";

    res.json({ success: true, message: "Sync erfolgreich", rank });
  } catch (e) {
    console.error(e);
    res.status(500).json({ success: false, message: "Serverfehler" });
  }
});
