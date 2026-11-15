/** Rupee amount with paise only when needed: 590 -> "₹590", 160.5 -> "₹160.50". */
export function formatRupees(amount: number): string {
  const hasPaise = Math.round(amount * 100) % 100 !== 0;
  return `₹${amount.toFixed(hasPaise ? 2 : 0)}`;
}
