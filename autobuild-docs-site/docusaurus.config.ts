import { themes as prismThemes } from "prism-react-renderer";
import type { Config } from "@docusaurus/types";
import type * as Preset from "@docusaurus/preset-classic";

// This runs in Node.js - Don't use client-side code here (browser APIs, JSX...)

const config: Config = {
  title: "AutoBuild Docs",
  tagline: "Build your CI/CD pipelines with ease",
  favicon: "img/favicon.svg",

  // Future flags, see https://docusaurus.io/docs/api/docusaurus-config#future
  future: {
    v4: true, // Improve compatibility with the upcoming Docusaurus v4
  },

  // Set the production url of your site here
  url: "https://autobuilders.github.io",
  // Set the /<baseUrl>/ pathname under which your site is served
  // For GitHub pages deployment, it is often '/<projectName>/'
  baseUrl: "/AutoBuild/",

  // GitHub pages deployment config.
  // If you aren't using GitHub pages, you don't need these.
  organizationName: "AutoBuilders", // Usually your GitHub org/user name.
  projectName: "AutoBuild", // Usually your repo name.

  onBrokenLinks: "throw",

  // Even if you don't use internationalization, you can use this field to set
  // useful metadata like html lang. For example, if your site is Chinese, you
  // may want to replace "en" with "zh-Hans".
  i18n: {
    defaultLocale: "en",
    locales: ["en"],
  },

  presets: [
    [
      "classic",
      {
        docs: {
          path: "../autobuild-docs",
          sidebarPath: "./sidebars.ts",
          // Please change this to your repo.
          // Remove this to remove the "edit this page" links.
          editUrl: "https://github.com/AutoBuilders/AutoBuild/tree/main/",
        },
        theme: {
          customCss: "./src/css/custom.css",
        },
        blog: false,
      } satisfies Preset.Options,
    ],
  ],

  themeConfig: {
    // Replace with your project's social card
    image: "img/autobuild-social-card.jpg",
    colorMode: {
      respectPrefersColorScheme: true,
    },
    navbar: {
      title: "AutoBuild",
      logo: {
        alt: "AutoBuild Logo",
        src: "img/logo.svg", // TODO: Replace with our Logo
      },
      items: [
        {
          type: "docSidebar",
          sidebarId: "tutorialSidebar",
          position: "left",
          label: "Docs",
        },
        {
          href: "https://github.com/AutoBuilders/AutoBuild",
          label: "GitHub",
          position: "right",
        },
      ],
    },
    footer: {
      style: "dark",
      links: [
        {
          title: "Setup",
          items: [
            {
              label: "Getting Started",
              to: "/docs/setup/dev-env-setup",
            },
            {
              label: "Bazel Setup",
              to: "/docs/setup/bazel",
            },
            {
              label: "Local Backend Execution",
              to: "/docs/setup/backend/local-backend-execution",
            },
            {
              label: "Running Containers",
              to: "/docs/setup/backend/running-backend-containers",
            },
          ],
        },
        {
          title: "Architecture",
          items: [
            {
              label: "Architecture Overview",
              to: "/docs/architecture",
            },
          ],
        },
        {
          title: "More",
          items: [
            {
              label: "GitHub",
              href: "https://github.com/AutoBuilders/AutoBuild",
            },
            {
              label: "Report an Issue",
              href: "https://github.com/AutoBuilders/AutoBuild/issues",
            },
          ],
        },
        {
          title: "Builders",
          items: [
            {
              label: "Suvabrata",
              href: "https://www.linkedin.com/in/suvabrata-chowdhury/",
            },
            {
              label: "Sam",
              href: "https://www.linkedin.com/in/vivian-samuel-rodrigues-b73419226/",
            },
            {
              label: "Meenakshi",
              href: "https://www.linkedin.com/in/meenakshi-sarkar/",
            },
            {
              label: "BaiBhab",
              href: "https://www.linkedin.com/in/baibhab-dey-mldeveloper/",
            },
          ],
        }
      ],
      copyright: `Copyright © ${new Date().getFullYear()} AutoBuilders. Built with Docusaurus.`,
    },

    prism: {
      theme: prismThemes.github,
      darkTheme: prismThemes.dracula,
    },
  } satisfies Preset.ThemeConfig,
};

export default config;
