import Footer from "./components/Footer";
import NavBar from "./components/NavBar";
import {BrowserRouter, Route, Routes} from "react-router-dom";
import LandingPage from "./views/LandingPage";
import HomePage from "./views/HomePage";
import BrowseManifestsPage from "./views/BrowseManifestsPage";
import UploadManifestPage from "./views/UploadManifestPage";
import ProfilePage from "./views/ProfilePage";
import { useKeycloak } from "@react-keycloak/web";
import PrivateRoute from "./components/PrivateRoute";
import NodeStatus from "./components/NodeStatus";

function App() {
  const { keycloak, initialized } = useKeycloak();

  if (!initialized) {
      return <div>Loading...</div>;
  }

  return (
      <BrowserRouter>
          <div className="App">
              <NavBar/>
              <div className={"content"}>
                  <Routes>
                      <Route path="/" element={keycloak.authenticated ? <PrivateRoute children={<HomePage/>}/> : <LandingPage/>} />
                      <Route path="/browse" element={keycloak.authenticated ? <PrivateRoute children={<BrowseManifestsPage/>}/> : <LandingPage/>} />
                      <Route path="/upload" element={keycloak.authenticated ? <PrivateRoute children={<UploadManifestPage/>}/> : <LandingPage/>} />
                      <Route path="/profile" element={keycloak.authenticated ? <PrivateRoute children={<ProfilePage/>}/> : <LandingPage/>} />
                  </Routes>
              </div>
              <NodeStatus />
              <Footer/>
          </div>
      </BrowserRouter>
  );
}

export default App;
