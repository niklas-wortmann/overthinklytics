import * as React from 'react';
import { ArrowDownRight, ArrowUpRight } from 'lucide-react';

export function Trend({ value, label }: { value: number; label?: string }) {
  const positive = value >= 0;
  const formatted = `${positive ? '+' : ''}${value.toFixed(1)}%`;
  return (
    <span
      className={
        'inline-flex items-center gap-1 rounded-full px-2 py-0.5 text-xs font-medium ' +
        (positive
          ? 'bg-green-500/15 text-green-300 ring-1 ring-inset ring-green-500/30'
          : 'bg-red-500/15 text-red-300 ring-1 ring-inset ring-red-500/30')
      }
      aria-label={label ?? 'trend'}
    >
      {positive ? (
        <ArrowUpRight size={14} className="shrink-0" />
      ) : (
        <ArrowDownRight size={14} className="shrink-0" />
      )}
      {formatted}
    </span>
  );
}
