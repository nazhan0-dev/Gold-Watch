import { render, screen } from "@testing-library/react";
import Home from "./page";

it("renders the app name", () => {
  render(<Home />);
  expect(screen.getByRole("heading", { name: "GoldWatch" })).toBeInTheDocument();
});
