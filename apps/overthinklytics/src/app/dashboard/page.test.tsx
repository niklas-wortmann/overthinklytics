import React from 'react';
import { render, screen, waitFor } from '@testing-library/react';
import DashboardPage from './page';
import { vi} from 'vitest';

// Mock recharts with light-weight stand-ins to avoid complex SVG/layout in jsdom
vi.mock('recharts', () => {
  const Basic: React.FC<React.PropsWithChildren<any>> = ({ children, ...rest }) => (
    <div data-testid={rest['data-testid'] || 'recharts-mock'}>{children}</div>
  );
  return {
    ResponsiveContainer: Basic,
    LineChart: Basic,
    Line: Basic,
    XAxis: Basic,
    YAxis: Basic,
    Tooltip: Basic,
    CartesianGrid: Basic,
    BarChart: Basic,
    Bar: Basic,
    AreaChart: Basic,
    Area: Basic,
    PieChart: Basic,
    Pie: Basic,
    Cell: Basic,
    Legend: Basic,
  };
});

// Mock API client to control responses
vi.mock('@/lib/apiClient', async () => {
  const original: any = await vi.importActual('@/lib/apiClient');
  return {
    ...original,
    apiFetch: vi.fn(),
  };
});

import { apiFetch } from '@/lib/apiClient';

describe('DashboardPage', () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  it('renders the heading', () => {
    // Arrange: keep api calls pending so loading state is visible initially
    (apiFetch as any).mockImplementation(() => new Promise(() => {}));

    render(<DashboardPage />);

    expect(screen.getByRole('heading', { name: /analytics overview/i })).toBeInTheDocument();
    expect(screen.getByText(/loading analytics/i)).toBeInTheDocument();
  });

  it('loads and displays KPI cards and chart titles on success', async () => {
    // Arrange successful responses in the expected order of calls
    // 1) /analytics/kpis
    (apiFetch as any)
      .mockResolvedValueOnce({ ok: true, status: 200, data: { kpis: [
        { label: 'Total Users', value: '1,234', delta: 12 },
        { label: 'Sessions', value: '3,210', delta: -3 },
        { label: 'Conversion', value: '4.2%', delta: 1.2 },
        { label: 'Revenue', value: '$12,345', delta: 8 },
      ] }})
      // 2) /analytics/traffic
      .mockResolvedValueOnce({ ok: true, status: 200, data: { data: [
        { day: '2025-10-01', visits: 100, sessions: 80 },
        { day: '2025-10-02', visits: 120, sessions: 90 },
      ] }})
      // 3) /analytics/signups
      .mockResolvedValueOnce({ ok: true, status: 200, data: { data: [
        { channel: 'Organic', signups: 50 },
        { channel: 'Paid', signups: 30 },
      ] }})
      // 4) /analytics/revenue
      .mockResolvedValueOnce({ ok: true, status: 200, data: { data: [
        { day: '2025-10-01', value: 1000 },
        { day: '2025-10-02', value: 1500 },
      ] }})
      // 5) /analytics/device-share
      .mockResolvedValueOnce({ ok: true, status: 200, data: { data: [
        { name: 'Desktop', value: 60 },
        { name: 'Mobile', value: 40 },
      ] }});

    render(<DashboardPage />);

    // Loading should disappear
    await waitFor(() => expect(screen.queryByText(/loading analytics/i)).not.toBeInTheDocument());

    // KPI cards
    expect(screen.getByText('Total Users')).toBeInTheDocument();
    expect(screen.getByText('Sessions')).toBeInTheDocument();
    expect(screen.getByText('Conversion')).toBeInTheDocument();
    expect(screen.getByText('Revenue')).toBeInTheDocument();
    // Values
    expect(screen.getByText('1,234')).toBeInTheDocument();
    expect(screen.getByText('3,210')).toBeInTheDocument();
    expect(screen.getByText('4.2%')).toBeInTheDocument();
    expect(screen.getByText('$12,345')).toBeInTheDocument();

    // Chart titles are present
    expect(screen.getByText(/traffic \(visits vs sessions\)/i)).toBeInTheDocument();
    expect(screen.getByText(/signups by channel/i)).toBeInTheDocument();
    expect(screen.getByText(/revenue/i)).toBeInTheDocument();
    expect(screen.getByText(/device share/i)).toBeInTheDocument();
  });

  it('shows an error message when any API call fails', async () => {
    (apiFetch as any)
      .mockResolvedValueOnce({ ok: false, status: 500, error: 'Server error' })
      .mockResolvedValueOnce({ ok: true, status: 200, data: { data: [] } })
      .mockResolvedValueOnce({ ok: true, status: 200, data: { data: [] } })
      .mockResolvedValueOnce({ ok: true, status: 200, data: { data: [] } })
      .mockResolvedValueOnce({ ok: true, status: 200, data: { data: [] } });

    render(<DashboardPage />);

    await waitFor(() => expect(screen.queryByText(/loading analytics/i)).not.toBeInTheDocument());

    // Error message should be displayed
    expect(screen.getByText(/server error/i)).toBeInTheDocument();
  });
});
