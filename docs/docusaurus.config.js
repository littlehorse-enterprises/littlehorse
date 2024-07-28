// @ts-check
// Note: type annotations allow type checking and IDEs autocompletion

const {themes} = require('prism-react-renderer');
const lightCodeTheme = themes.github;
// const darkTheme = themes.dracula;

/** @type {import('@docusaurus/types').Config} */
module.exports = {
  title: "LittleHorse Orchestrator",
  tagline: "By Engineers, For Engineers",
  favicon: "img/logo.jpg",

  // Set the production url of your site here
  url: "https://littlehorse.dev",
  // Set the /<baseUrl>/ pathname under which your site is served
  // For GitHub pages deployment, it is often '/<projectName>/'
  baseUrl: "/",

  // // GitHub pages deployment config.
  // // If you aren't using GitHub pages, you don't need these.
  // organizationName: 'facebook', // Usually your GitHub org/user name.
  // projectName: 'docusaurus', // Usually your repo name.

  onBrokenLinks: "throw",
  onBrokenMarkdownLinks: "warn",

  // Even if you don't use internalization, you can use this field to set useful
  // metadata like html lang. For example, if your site is Chinese, you may want
  // to replace "en" with "zh-Hans".
  i18n: {
    defaultLocale: "en",
    locales: ["en"],
  },

  presets: [
    [
      'classic',
      {
        docs: {
          sidebarPath: require.resolve("./sidebars.js"),
        },
        blog: {},
        theme: {
          customCss: require.resolve("./src/css/custom.css"),
        },
      },
    ],
  ],

  themeConfig:
    /** @type {import('@docusaurus/preset-classic').ThemeConfig} */
    ({
      // Replace with your project's social card
      image: "img/docusaurus-social-card.jpg",
      colorMode: {
        disableSwitch: true,
      },
      navbar: {
        title: "LittleHorse",
        logo: {
          alt: "LittleHorse Logo",
          src: "img/logo.jpg",
        },
        items: [
          {
            type: "docSidebar",
            sidebarId: "tutorialSidebar",
            position: "left",
            label: "Docs",
          },
          {
            to: "/blog",
            position: "left",
            label: "Blog",
          },
          {
            href: "https://littlehorse.io",
            position: "left",
            label: "Product Site",
          },
          {
            href: "https://docs.google.com/forms/d/e/1FAIpQLScXVvTYy4LQnYoFoRKRQ7ppuxe0KgncsDukvm96qKN0pU5TnQ/viewform?usp=sf_link",
            label: "Contact Us",
            position: "right",
          },
          {
            href: "https://github.com/littlehorse-enterprises/littlehorse",
            label: "GitHub",
            position: "right",
          },
          {
            href: "https://launchpass.com/littlehorsecommunity",
            label: "Slack",
            position: "right",
          },
        ],
      },
      footer: {
        links: [
          {
            title: "Docs",
            items: [
              {
                label: "Concepts",
                to: "/docs/concepts",
              },
              {
                label: "Developer Guide",
                to: "/docs/developer-guide",
              },
            ],
          },
          {
            title: "More",
            items: [
              {
                label: "Blog",
                to: "/blog",
              },
              {
                label: "Slack",
                href: "https://launchpass.com/littlehorsecommunity",
              },
              {
                label: "GitHub",
                href: "https://github.com/littlehorse-enterprises/littlehorse",
              },
            ],
          },
        ],
        copyright: `Copyright © ${new Date().getFullYear()} LittleHorse Enterprises LLC.`,
      },
      prism: {
        theme: lightCodeTheme,
        additionalLanguages: ["java", "go", "groovy", "protobuf"],
      },
    }),
};
