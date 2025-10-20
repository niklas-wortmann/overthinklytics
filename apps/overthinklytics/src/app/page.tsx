import { redirect } from 'next/navigation';

export default function IndexPage() {
  // Redirect root path to the dashboard
  redirect('/dashboard');
}
