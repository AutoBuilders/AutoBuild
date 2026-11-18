import type {ReactNode} from 'react';
import clsx from 'clsx';
import Heading from '@theme/Heading';
import styles from './styles.module.css';

type FeatureItem = {
  title: string;
  icon: ReactNode;
  description: ReactNode;
};

const FeatureList: FeatureItem[] = [
  {
    title: 'Low-code Pipeline Builder',
    icon: (
      <svg viewBox="0 0 80 80" width="80" height="80" xmlns="http://www.w3.org/2000/svg">
        <defs>
          <linearGradient id="g1" x1="0%" y1="0%" x2="100%" y2="100%">
            <stop offset="0%" stopColor="#3b82f6"/>
            <stop offset="100%" stopColor="#8b5cf6"/>
          </linearGradient>
        </defs>
        <rect x="4" y="28" width="20" height="24" rx="5" fill="none" stroke="url(#g1)" strokeWidth="2.5"/>
        <rect x="30" y="28" width="20" height="24" rx="5" fill="none" stroke="url(#g1)" strokeWidth="2.5"/>
        <rect x="56" y="28" width="20" height="24" rx="5" fill="url(#g1)" fillOpacity="0.15" stroke="url(#g1)" strokeWidth="2.5"/>
        <line x1="24" y1="40" x2="30" y2="40" stroke="#3b82f6" strokeWidth="2"/>
        <line x1="50" y1="40" x2="56" y2="40" stroke="#8b5cf6" strokeWidth="2"/>
        <polyline points="64,35 68,40 64,45" fill="none" stroke="#8b5cf6" strokeWidth="2.5" strokeLinecap="round" strokeLinejoin="round"/>
      </svg>
    ),
    description: (
      <>
        Define your entire pipeline from the UI — stages, scripts, and triggers — without writing complex config files by hand.
      </>
    ),
  },
  {
    title: 'VCS Integration',
    icon: (
      <svg viewBox="0 0 80 80" width="80" height="80" xmlns="http://www.w3.org/2000/svg">
        <defs>
          <linearGradient id="g2" x1="0%" y1="0%" x2="100%" y2="100%">
            <stop offset="0%" stopColor="#3b82f6"/>
            <stop offset="100%" stopColor="#8b5cf6"/>
          </linearGradient>
        </defs>
        <circle cx="20" cy="20" r="8" fill="none" stroke="url(#g2)" strokeWidth="2.5"/>
        <circle cx="20" cy="60" r="8" fill="none" stroke="url(#g2)" strokeWidth="2.5"/>
        <circle cx="60" cy="36" r="8" fill="url(#g2)" fillOpacity="0.15" stroke="url(#g2)" strokeWidth="2.5"/>
        <line x1="20" y1="28" x2="20" y2="52" stroke="#3b82f6" strokeWidth="2"/>
        <path d="M20 20 Q40 20 52 30" fill="none" stroke="#8b5cf6" strokeWidth="2" strokeDasharray="4,3"/>
        <path d="M20 60 Q40 60 52 42" fill="none" stroke="#8b5cf6" strokeWidth="2" strokeDasharray="4,3"/>
      </svg>
    ),
    description: (
      <>
        Connect your VCS and trigger pipelines automatically on push, commit, or a custom schedule. Build status flows back to your repo.
      </>
    ),
  },
  {
    title: 'Real-time Build Visibility',
    icon: (
      <svg viewBox="0 0 80 80" width="80" height="80" xmlns="http://www.w3.org/2000/svg">
        <defs>
          <linearGradient id="g3" x1="0%" y1="0%" x2="100%" y2="100%">
            <stop offset="0%" stopColor="#3b82f6"/>
            <stop offset="100%" stopColor="#8b5cf6"/>
          </linearGradient>
        </defs>
        <rect x="8" y="14" width="64" height="44" rx="6" fill="none" stroke="url(#g3)" strokeWidth="2.5"/>
        <line x1="8" y1="26" x2="72" y2="26" stroke="url(#g3)" strokeWidth="1.5" strokeOpacity="0.4"/>
        <rect x="14" y="32" width="24" height="6" rx="3" fill="#3b82f6" fillOpacity="0.3"/>
        <rect x="14" y="32" width="18" height="6" rx="3" fill="#3b82f6"/>
        <rect x="14" y="43" width="40" height="6" rx="3" fill="#8b5cf6" fillOpacity="0.3"/>
        <rect x="14" y="43" width="40" height="6" rx="3" fill="#8b5cf6"/>
        <circle cx="58" cy="35" r="6" fill="#10b981" fillOpacity="0.2" stroke="#10b981" strokeWidth="2"/>
        <polyline points="55,35 57,37 61,32" fill="none" stroke="#10b981" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"/>
        <line x1="32" y1="58" x2="48" y2="58" stroke="url(#g3)" strokeWidth="2.5" strokeLinecap="round"/>
      </svg>
    ),
    description: (
      <>
        See live stage progress, logs, and build status directly in the app — or right in your VCS without switching context.
      </>
    ),
  },
];

function Feature({title, icon, description}: FeatureItem) {
  return (
    <div className={clsx('col col--4')}>
      <div className="text--center" style={{padding: '1rem 0'}}>
        {icon}
      </div>
      <div className="text--center padding-horiz--md">
        <Heading as="h3">{title}</Heading>
        <p>{description}</p>
      </div>
    </div>
  );
}

export default function HomepageFeatures(): ReactNode {
  return (
    <section className={styles.features}>
      <div className="container">
        <div className="row">
          {FeatureList.map((props, idx) => (
            <Feature key={idx} {...props} />
          ))}
        </div>
      </div>
    </section>
  );
}
