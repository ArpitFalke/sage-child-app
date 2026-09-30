import { NextResponse } from "next/server";

// For prototype purposes, we store data in a global variable.
// In production, you would use Vercel KV, Redis, or Firebase.
global.childData = global.childData || { lat: 0, lng: 0, usage: "Waiting for data...", timestamp: null };

export async function POST(req) {
  try {
    const body = await req.json();
    global.childData = {
      lat: body.lat,
      lng: body.lng,
      usage: body.usage,
      timestamp: new Date().toISOString()
    };
    return NextResponse.json({ success: true, message: "Data received" });
  } catch (error) {
    return NextResponse.json({ success: false, error: error.message }, { status: 500 });
  }
}
