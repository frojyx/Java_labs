import { KeyboardEvent, useState } from "react";
import { X } from "lucide-react";
import { Badge } from "@/components/ui/badge";
import { Input } from "@/components/ui/input";

interface MultiValueInputProps {
  label: string;
  values: string[];
  placeholder: string;
  suggestions?: string[];
  onChange: (values: string[]) => void;
}

export function MultiValueInput({
  label,
  values,
  placeholder,
  suggestions = [],
  onChange,
}: MultiValueInputProps) {
  const [draft, setDraft] = useState("");

  function addValue(value: string) {
    const normalizedValue = value.trim();
    if (!normalizedValue || values.includes(normalizedValue)) {
      return;
    }
    onChange([...values, normalizedValue]);
    setDraft("");
  }

  function handleKeyDown(event: KeyboardEvent<HTMLInputElement>) {
    if (event.key === "Enter" || event.key === ",") {
      event.preventDefault();
      addValue(draft);
    }
  }

  return (
    <div className="space-y-3">
      <div className="text-sm font-medium text-foreground/90">{label}</div>
      <Input
        value={draft}
        list={`${label}-suggestions`}
        placeholder={placeholder}
        onChange={(event) => setDraft(event.target.value)}
        onKeyDown={handleKeyDown}
      />
      <datalist id={`${label}-suggestions`}>
        {suggestions.map((suggestion) => (
          <option key={suggestion} value={suggestion} />
        ))}
      </datalist>
      <div className="flex flex-wrap gap-2">
        {values.map((value) => (
          <Badge key={value} variant="outline" className="gap-2 rounded-full pr-2">
            {value}
            <button
              type="button"
              className="rounded-full p-0.5 transition hover:bg-secondary"
              onClick={() => onChange(values.filter((item) => item !== value))}
            >
              <X className="h-3 w-3" />
            </button>
          </Badge>
        ))}
      </div>
    </div>
  );
}
