import { NextResponse, NextRequest } from 'next/server'

// Apply CORS to all API routes
export function middleware(request: NextRequest) {
  const { pathname } = request.nextUrl

  if (pathname.startsWith('/api')) {
    const corsHeaders = {
      'Access-Control-Allow-Origin': '*',
      'Access-Control-Allow-Methods': 'GET, POST, PUT, PATCH, DELETE, OPTIONS',
      'Access-Control-Allow-Headers': '*',
      'Access-Control-Max-Age': '86400',
    }

    if (request.method === 'OPTIONS') {
      return new NextResponse(null, { status: 204, headers: corsHeaders })
    }

    const response = NextResponse.next()
    Object.entries(corsHeaders).forEach(([key, value]) => response.headers.set(key, value))
    return response
  }

  return NextResponse.next()
}

export const config = {
  matcher: ['/api/:path*'],
}
