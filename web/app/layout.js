export const metadata = {
  title: "SAGE Child Care Dashboard",
  description: "Monitor your child's location and app usage",
};

export default function RootLayout({ children }) {
  return (
    <html lang="en">
      <body style={{ fontFamily: 'system-ui, sans-serif', margin: 0, padding: 0, backgroundColor: '#f0f4f8' }}>
        {children}
      </body>
    </html>
  );
}
