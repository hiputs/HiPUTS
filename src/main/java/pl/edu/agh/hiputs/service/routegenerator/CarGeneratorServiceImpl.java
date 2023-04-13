package pl.edu.agh.hiputs.service.routegenerator;

import lombok.AllArgsConstructor;
import pl.edu.agh.hiputs.model.map.patch.Patch;
import pl.edu.agh.hiputs.service.routegenerator.generator.CarGenerator;

@AllArgsConstructor
public class CarGeneratorServiceImpl implements CarGeneratorService{

    private final CarGenerator carGenerator;

    @Override
    public void generateRoutes(Patch patch) {
        throw new UnimplementedException();
    }
}
