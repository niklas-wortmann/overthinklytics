"use client";

import React from 'react';
import { BackendSwitcher } from '@/components/dev/BackendSwitcher';
import { DebugInfoToggle } from '@/components/system/DebugInfoToggle';

// Read compile-time public flags (inlined by Next.js)
const SHOW_SWITCHER = (process.env.NEXT_PUBLIC_SHOW_BACKEND_SWITCHER || '')
  .toString()
  .toLowerCase();
const SHOW_DEBUG = (process.env.NEXT_PUBLIC_SHOW_DEBUG_TOGGLE || '')
  .toString()
  .toLowerCase();

const shouldShowSwitcher = SHOW_SWITCHER === '1' || SHOW_SWITCHER === 'true';
const shouldShowDebug = SHOW_DEBUG === '1' || SHOW_DEBUG === 'true';

export default function ClientWidgets() {
  // Mount gate: ensure no HTML is emitted during SSR to avoid hydration drift
  const [mounted, setMounted] = React.useState(false);
  React.useEffect(() => setMounted(true), []);
  if (!mounted) return null;

  return (
    <>
      {shouldShowDebug ? <DebugInfoToggle /> : null}
      {shouldShowSwitcher ? <BackendSwitcher /> : null}
    </>
  );
}
