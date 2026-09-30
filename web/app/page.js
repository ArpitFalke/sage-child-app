"use client";
import { useEffect, useState, useRef } from "react";
import Peer from "peerjs";
import { Html5Qrcode } from "html5-qrcode";
import { MapPin, Clock, Camera, Link2, AlertTriangle, Smartphone } from "lucide-react";

export default function Dashboard() {
  const [peer, setPeer] = useState(null);
  const [connection, setConnection] = useState(null);
  const [data, setData] = useState(null);
  const [scanning, setScanning] = useState(false);
  const scannerRef = useRef(null);

  // Initialize Parent Peer
  useEffect(() => {
    const p = new Peer();
    setPeer(p);

    p.on("connection", (conn) => {
      setConnection(conn);
      conn.on("data", (payload) => {
        setData(payload);
      });
    });

    return () => p.destroy();
  }, []);

  // Start scanning logic
  useEffect(() => {
    if (scanning && scannerRef.current) {
      const scanner = new Html5Qrcode(scannerRef.current.id);
      scanner.start(
        { facingMode: "environment" },
        { fps: 10, qrbox: 250 },
        (decodedText) => {
          // On scan success, connect to child's Peer ID
          if (peer && !connection) {
            const conn = peer.connect(decodedText);
            conn.on("data", (payload) => setData(payload));
            setConnection(conn);
          }
          scanner.stop();
          setScanning(false);
        },
        (err) => console.warn("Scan error", err)
      );
    }
  }, [scanning, peer, connection]);

  return (
    <main style={{ maxWidth: "800px", margin: "0 auto", padding: "20px", fontFamily: 'system-ui, sans-serif', backgroundColor: '#f8fafc', minHeight: '100vh' }}>
      <header style={{ display: 'flex', alignItems: 'center', gap: '12px', marginBottom: '24px' }}>
        <Smartphone size={32} color="#2563eb" />
        <h1 style={{ margin: 0, color: "#1e293b", fontSize: '24px' }}>SAGE Parent Dashboard</h1>
      </header>

      {!connection ? (
        <div style={{ background: 'white', padding: '32px', borderRadius: '12px', boxShadow: '0 4px 6px rgba(0,0,0,0.05)', textAlign: 'center' }}>
          <Camera size={48} color="#64748b" style={{ margin: '0 auto 16px' }} />
          <h2 style={{ color: "#1e293b" }}>Connect to Child Device</h2>
          <p style={{ color: "#64748b", marginBottom: '24px' }}>Scan the QR code displayed on your child's phone to establish a secure connection.</p>
          <button 
            onClick={() => setScanning(!scanning)}
            style={{ background: '#2563eb', color: 'white', border: 'none', padding: '12px 24px', borderRadius: '8px', cursor: 'pointer', display: 'inline-flex', alignItems: 'center', gap: '8px', fontSize: '16px' }}
          >
            <Link2 size={20} />
            {scanning ? "Stop Scanning" : "Start Scanning"}
          </button>
          {scanning && <div id="qr-reader" ref={scannerRef} style={{ marginTop: '24px', width: '100%' }}></div>}
        </div>
      ) : (
        <div style={{ display: 'grid', gap: '16px' }}>
          <div style={{ background: 'white', padding: '20px', borderRadius: '12px', boxShadow: '0 4px 6px rgba(0,0,0,0.05)' }}>
            <div style={{ display: 'flex', alignItems: 'center', gap: '8px', marginBottom: '12px' }}>
              <MapPin size={20} color="#ef4444" />
              <h2 style={{ margin: 0, color: "#1e293b", fontSize: '18px' }}>Live Location</h2>
            </div>
            {data && data.lat ? (
              <div>
                <p style={{ color: '#334155', margin: '4px 0' }}>Lat: {data.lat}</p>
                <p style={{ color: '#334155', margin: '4px 0' }}>Lng: {data.lng}</p>
                <a href={`https://www.google.com/maps?q=${data.lat},${data.lng}`} target="_blank" rel="noopener noreferrer" style={{ color: '#2563eb', textDecoration: 'underline', marginTop: '8px', display: 'inline-block' }}>
                  View on Google Maps
                </a>
              </div>
            ) : <p style={{ color: '#64748b' }}>Waiting for location...</p>}
          </div>

          <div style={{ background: 'white', padding: '20px', borderRadius: '12px', boxShadow: '0 4px 6px rgba(0,0,0,0.05)' }}>
            <div style={{ display: 'flex', alignItems: 'center', gap: '8px', marginBottom: '12px' }}>
              <Clock size={20} color="#f59e0b" />
              <h2 style={{ margin: 0, color: "#1e293b", fontSize: '18px' }}>App Usage</h2>
            </div>
            {data && data.usage && data.usage !== "Waiting for data..." ? (
              <pre style={{ whiteSpace: "pre-wrap", fontFamily: "inherit", fontSize: "14px", color: '#334155' }}>{data.usage}</pre>
            ) : <p style={{ color: '#64748b' }}>No usage data reported yet.</p>}
          </div>
        </div>
      )}
    </main>
  );
}
