// @ts-check
// Note: type annotations allow type checking and IDEs autocompletion

const {themes} = require('prism-react-renderer');
const lightCodeTheme = themes.github;
// const darkTheme = themes.dracula;

/** @type {import('@docusaurus/types').Config} */
module.exports = {
  title: "LittleHorse Orchestrator",
  tagline: "The Missing Piece of the Microservice Architecture",
  favicon: "img/logo.png",

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

  // Colt gets very grumpy when links don't work.
  onBrokenMarkdownLinks: "throw",
  onBrokenAnchors: "throw",

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
        googleTagManager: {
          containerId: "GTM-NCK3N2PC",
        },
        gtag: {
          trackingID: 'G-1DL56CH5SS',
        },
        blog: {
            blogSidebarCount: 'ALL',
            blogTitle: 'LittleHorse OSS Blog',
            blogDescription: 'The latest news and analysis from your favorite workflow engine.',
            postsPerPage: 20,
        },
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
          src: "img/logo-brown.png",
          srcDark: "img/logo.png"
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
            className: 'header-github-link',
            position: "right",
          },
          {
            href: "https://launchpass.com/littlehorsecommunity",
            className: 'header-slack-link',
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
      algolia: {
        appId: 'G6OXK45P4J', // The application ID provided by Algolia
        apiKey: '038f9d47703c58c2c8abace8998eaed1', // Public API key: it is safe to commit it
        indexName: 'littlehorse',
        externalUrlRegex: "littlehorse\\.dev",
        contextualSearch: true, // Enforces context of language and version on search results
        searchPagePath: 'search', // path for search page that enabled by default
        insights: false,
      },
    }),
};
