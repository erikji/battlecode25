works = []
for dx in range(-5,6):
    for dy in range(-5,6):
        if dx*dx+dy*dy<=20:
            works += [(dx,dy)]
works.sort(key=lambda a:a[0]*a[0]+a[1]*a[1])
#mopper
# a = [(-1, -1), (0, -1), (1, -1), (-1, 0), (1, 0), (-1, 1), (0, 1), (1, 1)]
# for d in a:
#     asdf = []
#     s = ''
#     for dx in range(-1, 2):
#         for dy in range(-1, 2):
#             if (dx+d[0])**2 + (dy+d[1])**2 > 2:
#                 ind = works.index((dx+d[0],dy+d[1]))
#                 s += f'\t\tif (mopScores[{ind}] > allmax[{a.index(d)}])' + ' {\n'
#                 s += f'\t\t\tallmax[{a.index(d)}] = mopScores[{ind}];\n'
#                 s += f'\t\t\tallx[{a.index(d)}] = {dx+d[0]};\n'
#                 s += f'\t\t\tally[{a.index(d)}] = {dy+d[1]};\n'
#                 s += '\t\t}\n'
#                 # asdf += [works.index((dx+d[0],dy+d[1]))]
#     # print(d,asdf)
#     print(s,end='')
for i in range(25):
    print("""\t\tloc = G.me.translate("""+str(works[i][0])+""", """+str(works[i][1])+""");
        if (G.rc.onTheMap(loc) && G.rc.senseMapInfo(loc).getPaint().isEnemy()) {
            if (G.rc.canSenseRobotAtLocation(loc)) {
                //if it's an opponent, they get -1 paint
                //if it's an ally, they go from -2 to -1 paint
                //in both cases we gain 1 paint
                //can't be a tower because it has to be painted
                RobotInfo bot = G.rc.senseRobotAtLocation(loc);
                // mopScores["""+str(i)+"""] += (1 - bot.paintAmount / (double) bot.type.paintCapacity) * 10;
                mopScores["""+str(i)+"""] += 11 + Math.min(5, UnitType.MOPPER.paintCapacity - G.rc.getPaint());
                if (bot.getType() == UnitType.MOPPER) {
                    //double passive paint loss on moppers
                    mopScores["""+str(i)+"""]++;
                }
            }
            mopScores["""+str(i)+"""] += 5;
        }""")