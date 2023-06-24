import Image from "next/image";
import { Button, Header } from "ui";

export default function Page() {
  return (
    <>
    <div className="login">
      <div className="login_horse"></div>
      <div className="login_form">
        <Image src="./logo-lh.svg" alt="logo-lh" width={218} height={80}/>
        <form>
          <div className="title">
            Welcome to your
            <span className="display-block">Little Horse</span>
            <span className="mb-40 display-block color-primary">dashboard</span>
          </div>
          <button className="login-button">
            <div>
              <Image src="./key.svg" width={22} height={12} alt="login" />
            </div>
            Log in using SSO
          </button>
        </form>
        <div className="legals">Copyright © 2023 LittleHorse Enterprises LLC. </div>
      </div>

    </div>
    </>
  );
}
