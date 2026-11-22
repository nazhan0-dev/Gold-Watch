import { act, render, screen, waitFor, within } from "@testing-library/react";
import { renderHook } from "@testing-library/react";
import { usePolling } from "@/hooks/usePolling";
import { AnnouncementTable } from "./AnnouncementTable";
import { ChangeIndicator } from "./ChangeIndicator";
import { PriceCard } from "./PriceCard";

const MINUS = "−";

describe("ChangeIndicator", () => {
  it("shows arrow, sign and words, not just colour", () => {
    const { container } = render(<ChangeIndicator change="-150.00" changePercent="-0.26" />);

    expect(container).toHaveTextContent(`▼ลดลง${MINUS}150.00(${MINUS}0.26%)`);
    expect(screen.getByText("▼")).toHaveAttribute("aria-hidden", "true");
  });

  it("explains when there is nothing to compare with", () => {
    render(<ChangeIndicator change={null} />);
    expect(screen.getByText("ยังไม่มีข้อมูลเทียบ")).toBeInTheDocument();
  });
});

describe("PriceCard", () => {
  it("renders the formatted price, footnote and change; decoration is hidden from assistive tech", () => {
    const { container } = render(
      <PriceCard
        title="ทองคำแท่ง"
        badge="96.5%"
        label="ขายออก (บาท)"
        value="57150.00"
        footnote="รับซื้อ 56,950.00"
        change="150.00"
        changePercent="0.26"
        decoration="bar"
      />,
    );

    expect(screen.getByRole("heading", { name: "ทองคำแท่ง" })).toBeInTheDocument();
    expect(screen.getByText("57,150.00")).toBeInTheDocument();
    expect(screen.getByText("รับซื้อ 56,950.00")).toBeInTheDocument();
    expect(screen.getByText("+150.00")).toBeInTheDocument();
    expect(container.querySelector('[aria-hidden="true"] span')).not.toBeNull();
  });
});

describe("AnnouncementTable", () => {
  it("lists rounds newest first with Thai times", () => {
    render(
      <AnnouncementTable
        loading={false}
        data={{
          date: "2026-11-22",
          rounds: [
            {
              round: 2,
              announcedAt: "2026-11-22T03:30:00Z",
              barBuy: "57050.00",
              barSell: "57250.00",
              ornamentBuy: "55901.20",
              ornamentSell: "58050.00",
              change: "100.00",
            },
            {
              round: 1,
              announcedAt: "2026-11-22T02:09:00Z",
              barBuy: "56950.00",
              barSell: "57150.00",
              ornamentBuy: "55803.96",
              ornamentSell: "57950.00",
              change: "0.00",
            },
          ],
        }}
      />,
    );

    const rows = screen.getAllByRole("row");
    expect(rows).toHaveLength(3); // header + 2 rounds
    expect(within(rows[1]).getByText("10:30 น.")).toBeInTheDocument();
    expect(within(rows[1]).getByText("57,250.00")).toBeInTheDocument();
    expect(within(rows[2]).getByText("09:09 น.")).toBeInTheDocument();
    expect(screen.getByText("22 พ.ย. 2569")).toBeInTheDocument();
  });

  it("shows an empty message when nothing was announced yet", () => {
    render(<AnnouncementTable loading={false} data={{ date: "2026-11-22", rounds: [] }} />);
    expect(screen.getByText("ยังไม่มีการประกาศราคาในวันนี้")).toBeInTheDocument();
  });
});

describe("usePolling", () => {
  beforeEach(() => jest.useFakeTimers());
  afterEach(() => jest.useRealTimers());

  it("keeps the last good data when a refresh fails, then recovers", async () => {
    const fetcher = jest
      .fn()
      .mockResolvedValueOnce({ price: "1" })
      .mockRejectedValueOnce(new Error("ไม่สามารถเชื่อมต่อเซิร์ฟเวอร์ได้"))
      .mockResolvedValueOnce({ price: "2" });

    const { result } = renderHook(() => usePolling(fetcher, 60_000));
    await waitFor(() => expect(result.current.data).toEqual({ price: "1" }));

    await act(async () => {
      jest.advanceTimersByTime(60_000);
    });
    await waitFor(() => expect(result.current.error).toBe("ไม่สามารถเชื่อมต่อเซิร์ฟเวอร์ได้"));
    expect(result.current.data).toEqual({ price: "1" });

    await act(async () => {
      jest.advanceTimersByTime(60_000);
    });
    await waitFor(() => expect(result.current.data).toEqual({ price: "2" }));
    expect(result.current.error).toBeNull();
  });
});
