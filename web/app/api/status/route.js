import { NextResponse } from "next/server";

export async function GET() {
  return NextResponse.json(global.childData || { lat: 0, lng: 0, usage: "No data yet" });
}
