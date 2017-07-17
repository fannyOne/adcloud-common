package utils

import org.junit.Test
import org.yaml.snakeyaml.DumperOptions
import org.yaml.snakeyaml.Yaml

import static junit.framework.Assert.assertEquals

/**
 * Created by jacky on 16/7/12.
 */
class SnakeYamlTest {
    @Test
    void load_yaml() {
        Yaml yaml = new Yaml();
        String document = '''
env:
    - java6
    - java8
    - dotnet
    - nodejs
stages:
    rmp:
        type: rmp
        dependent_on: None
        script:
            - shell <>
            - git clone <http://20.26>
    download:
        type: download
        git_clone: <>
        script:
            - git clone <http://20.26>
            - git clone http://20.26
            - 0.5 # sequences can contain disparate types.
    build:
        type: build
        dependent_on: download
        maven:
          - compile
          - test
        gradle:
          - test
        script:
            - git clone http://20.26
            - mvn test -Pdev
    package:
        type: package
        dependent_on: build
        need_manual: false
        artifacts:
          - path: <>
          - zip: true
        script:
            - git clone http://20.26
            - mvn package -Pqa
    deploy:
        type: deploy
        dependent_on: package
        need_manual: true
        target:
          - url: <>
          - path: <>
          - app_id: <>
        script:
            - dcos.sh
            - dcos.py
'''
        def result = yaml.load(document);
        println(result);

        assert result.env == ['java6', 'java8', 'dotnet', 'nodejs']
        assert result.stages.rmp.script.size() == 2
        assert result.stages.rmp.script[0] == 'shell <>'
        assert result.stages.deploy.dependent_on == 'package'

    }

    @Test
    void dump_yaml() {
        Map<String, Object> data = new HashMap<String, Object>();
        data.put("name", "Silenthand Olleander");
        data.put("race", "Human");
        data.put("traits", ["ONE_HAND", "ONE_EYE"] as List);

        DumperOptions options = new DumperOptions();
        options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
        options.setIndicatorIndent(2);
        Yaml yaml = new Yaml(options);
        StringWriter writer = new StringWriter();

        yaml.dump(data, writer);

        System.out.println(writer.toString());
        def expectedYamlStr = '''traits:
  - ONE_HAND
  - ONE_EYE
race: Human
name: Silenthand Olleander
'''

        assertEquals expectedYamlStr, writer.toString()
    }
}
