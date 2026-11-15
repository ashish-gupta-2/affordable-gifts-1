import type { Packaging } from "../types";

interface Props {
  packagingOptions: Packaging[];
  selectedPackaging: Packaging | null;
  onSelect: (packaging: Packaging) => void;
}

const GROUPS: { kind: Packaging["kind"]; title: string }[] = [
  { kind: "box", title: "🎁 Gift Box" },
  { kind: "jar", title: "🫙 Glass Jars" },
];

export default function PackagingSelector({
  packagingOptions,
  selectedPackaging,
  onSelect,
}: Props) {
  return (
    <section className="mb-10">
      <div className="mb-6">
        <h2 className="text-2xl font-bold">📦 Choose Packaging</h2>
        <p className="text-gray-600">
          Each partition or jar holds one product
        </p>
      </div>

      <div className="space-y-6">
        {GROUPS.map((group) => {
          const options = packagingOptions.filter(
            (option) => option.kind === group.kind
          );
          if (options.length === 0) return null;

          return (
            <div key={group.kind}>
              <h3 className="mb-3 font-semibold text-gray-700">
                {group.title}
              </h3>

              <div className="grid gap-4 md:grid-cols-3">
                {options.map((packaging) => {
                  const isSelected =
                    selectedPackaging?.id === packaging.id;

                  return (
                    <button
                      key={packaging.id}
                      type="button"
                      aria-pressed={isSelected}
                      onClick={() => onSelect(packaging)}
                      className={`rounded-2xl border-2 bg-white p-5 text-left shadow-sm transition hover:shadow-md ${
                        isSelected
                          ? "border-green-600 bg-green-50"
                          : "border-gray-200"
                      }`}
                    >
                      <div className="flex items-center justify-between">
                        <span className="font-semibold text-gray-900">
                          {packaging.name}
                        </span>
                        <span className="font-bold text-green-700">
                          ₹{packaging.price}
                        </span>
                      </div>

                      <p className="mt-1 text-sm text-gray-500">
                        {packaging.description}
                      </p>
                    </button>
                  );
                })}
              </div>
            </div>
          );
        })}
      </div>
    </section>
  );
}
