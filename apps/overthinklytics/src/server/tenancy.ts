// Multitenancy removed: this module is deprecated.
export async function resolveAllowedTenantIds(): Promise<string[]> {
  throw new Error('Multitenancy has been removed');
}

