const priceFormatter = new Intl.NumberFormat('en-AE', {
  style: 'currency',
  currency: 'AED',
  minimumFractionDigits: 2,
});

/** 189 -> "AED 189.00" */
export function formatPrice(value) {
  return priceFormatter.format(Number(value ?? 0));
}

export const LOW_STOCK_LIMIT = 5;

/** Turn any failed request into one friendly sentence. */
export function getErrorMessage(error) {
  if (!error?.response) {
    return 'Cannot reach the IdaMart server. Is the backend running on port 8080?';
  }
  const body = error.response.data;
  if (body?.fieldErrors) {
    return Object.values(body.fieldErrors).join(' ');
  }
  return body?.message || 'Something went wrong. Please try again.';
}
