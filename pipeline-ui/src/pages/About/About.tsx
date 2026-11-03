import NavBar from "../../components/common/navBar";

export default function About() {
  const team = [
    { name: "Suvabrata Chowdhury", img: "/img/suva.jpg" },
    { name: "Vivian Rodrigues", img: "/img/sam.jpg" },
    { name: "Meenakshi Sarkar", img: "/img/meena.jpg" },
    { name: "Baibhab Dey", img: "/img/baibhab.jpg" },
  ];

  return (
    <>
      <NavBar></NavBar>
      <div className="w-full min-h-screen py-10 px-5 text-center font-sans bg-gradient-to-br from-[#e9f0ff] via-white to-[#dbe9ff] animate-gradient">
        {/* Hero */}
        <div className="mt-[30px] mb-[50px]">
          <h1 className="text-[42px] font-extrabold text-[#23406b] mb-[10px]">Automation Made Simple</h1>
          <p className="text-lg text-[#3f5b85] opacity-90">
            Streamlining processes with intelligence and efficiency.
          </p>
        </div>

        {/* Mission Section */}
        <div className="max-w-[900px] mx-auto mb-[60px] py-10 px-[50px] rounded-[22px] bg-white/75 backdrop-blur-[12px] shadow-[0_8px_24px_rgba(0,0,60,0.1)]">
          <h1 className="text-[32px] font-bold text-[#1f3d75] mb-[15px]">Our Mission</h1>
          <p className="text-lg leading-[1.7] text-[#3a4a63]">
            Our mission is to build an intelligent pipeline automation platform
            that simplifies workflows, enhances collaboration, and accelerates
            project delivery — all while maintaining clean design and
            user-friendly experiences. We aim to empower teams to focus on
            innovation rather than manual tasks.
          </p>
        </div>

        {/* Team Section */}
        <h2 className="text-[30px] font-bold mb-10 text-[#1f355a]">Meet the Team</h2>

        <div className="grid grid-cols-[repeat(auto-fit,minmax(230px,1fr))] gap-10 justify-items-center px-5">
          {team.map((member) => (
            <div
              key={member.name}
              className="w-60 bg-white/80 backdrop-blur-[10px] rounded-[20px] py-[25px] px-[15px] shadow-[0_6px_20px_rgba(0,0,50,0.15)] transition-all duration-[250ms] hover:-translate-y-[7px] hover:shadow-[0_10px_25px_rgba(0,0,70,0.18)]"
            >
              <img src={member.img} alt={member.name} className="w-[170px] h-[170px] rounded-full object-cover border-4 border-[#c3d7ff] mb-3" />
              <h3 className="text-lg font-semibold text-[#23406b]">{member.name}</h3>
            </div>
          ))}
        </div>
      </div>
    </>
  );
}
