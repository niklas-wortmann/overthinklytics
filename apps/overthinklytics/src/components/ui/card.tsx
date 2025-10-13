import * as React from 'react';

// Minimal shadcn-style Card primitives using Tailwind v4 tokens
// Usage: <Card><CardHeader>...</CardHeader><CardContent>...</CardContent></Card>

export function Card({ className = '', ...props }: React.ComponentProps<'div'>) {
  return (
    <div
      className={
        'rounded-xl border border-border/50 bg-card text-card-foreground shadow-sm ring-1 ring-inset ring-border/40 ' +
        className
      }
      {...props}
    />
  );
}

export function CardHeader({ className = '', ...props }: React.ComponentProps<'div'>) {
  return (
    <div className={'p-4 md:p-5 border-b border-border/50 ' + className} {...props} />
  );
}

export function CardTitle({ className = '', ...props }: React.ComponentProps<'h3'>) {
  return (
    <h3 className={'text-base md:text-lg font-semibold ' + className} {...props} />
  );
}

export function CardDescription({ className = '', ...props }: React.ComponentProps<'p'>) {
  return (
    <p className={'text-sm text-muted ' + className} {...props} />
  );
}

export function CardContent({ className = '', ...props }: React.ComponentProps<'div'>) {
  return <div className={'p-4 md:p-5 ' + className} {...props} />;
}

export function CardFooter({ className = '', ...props }: React.ComponentProps<'div'>) {
  return (
    <div className={'p-4 md:p-5 border-t border-border/50 ' + className} {...props} />
  );
}
