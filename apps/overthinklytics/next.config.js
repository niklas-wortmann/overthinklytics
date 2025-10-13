//@ts-check

// Load env files early and consistently (works under Nx as well)
// eslint-disable-next-line @typescript-eslint/no-var-requires
const { loadEnvConfig } = require('@next/env');
// Ensure .env, .env.local, etc. from this app directory are loaded
loadEnvConfig(__dirname);

// eslint-disable-next-line @typescript-eslint/no-var-requires
const { composePlugins, withNx } = require('@nx/next');

/**
 * @type {import('@nx/next/plugins/with-nx').WithNxOptions}
 **/
const nextConfig = {
  // Use this to set Nx-specific options
  // See: https://nx.dev/recipes/next/next-config-setup
  nx: {},
};

const plugins = [
  // Add more Next.js plugins to this list if needed.
  withNx,
];

module.exports = composePlugins(...plugins)(nextConfig);
