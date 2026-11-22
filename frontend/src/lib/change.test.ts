import { calcChange, changeDirection } from "./change";

describe("calcChange", () => {
  it("subtracts decimal strings exactly", () => {
    expect(calcChange("57150.00", "57000.00")).toBe("150.00");
    expect(calcChange("57000.00", "57150.00")).toBe("-150.00");
    expect(calcChange("0.3", "0.1")).toBe("0.2"); // 0.3 - 0.1 !== 0.19999999999999998
  });

  it("keeps the larger number of decimals", () => {
    expect(calcChange("3539.5", "3520.0000")).toBe("19.5000");
    expect(calcChange("33.9184", "33.8")).toBe("0.1184");
    expect(calcChange("1", "1")).toBe("0");
  });

  it("returns null when a side is missing", () => {
    expect(calcChange(null, "1.00")).toBeNull();
    expect(calcChange("1.00", undefined)).toBeNull();
  });

  it("rejects non-numeric input", () => {
    expect(() => calcChange("57,150.00", "1")).toThrow("Not a decimal number");
  });
});

describe("changeDirection", () => {
  it.each([
    ["150.00", "up"],
    ["-0.01", "down"],
    ["0.00", "flat"],
    ["-0", "flat"],
  ])("%s is %s", (input, expected) => {
    expect(changeDirection(input)).toBe(expected);
  });

  it("is null when unknown", () => {
    expect(changeDirection(null)).toBeNull();
  });
});
