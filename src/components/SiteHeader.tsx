interface Props {
  minOrderValue?: number;
}

export default function SiteHeader({ minOrderValue }: Props) {
  return (
    <header className="border-b border-gray-200 bg-white shadow-sm">
      <div className="mx-auto flex max-w-7xl items-center justify-between px-6 py-5">
        <div>
          <h1 className="text-3xl font-bold text-green-700">
            🎁 Affordable Gifts
          </h1>

          <p className="text-sm text-gray-500">
            Build your personalized dry fruit gift box
          </p>
        </div>

        {minOrderValue !== undefined && (
          <div className="rounded-full bg-green-100 px-4 py-2 text-sm font-medium text-green-700">
            Minimum Order ₹{minOrderValue}
          </div>
        )}
      </div>
    </header>
  );
}
