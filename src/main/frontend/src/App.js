import { BrowserRouter } from "react-router-dom";
import { InitialPage } from "./InitialPage";
import { Navigate, Route, Routes } from "react-router";

export const App = () => (
  <BrowserRouter>
    <Routes>
      <Route exact path={"/"} element={<Navigate to={"/initial-page"} />} />
      <Route exact path={"initial-page"} element={<InitialPage />} />
    </Routes>
  </BrowserRouter>
);
