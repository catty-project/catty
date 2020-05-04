/*
 * Copyright 2020 The Catty Project
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package pink.catty.test;

import junit.framework.Assert;
import org.junit.Test;
import pink.catty.core.meta.MetaInfo;
import pink.catty.core.meta.MetaType;

public class MetaInfoTest {

  @Test
  public void test0() {
    String mock0 = "0";
    int mock1 = 1;
    boolean mock2 = true;
    double mock3 = 2.0;

    MockMetaInfo mockMetaInfo = new MockMetaInfo();
    mockMetaInfo.setMock0("0");
    mockMetaInfo.setMock1(1);
    mockMetaInfo.setMock2(true);
    mockMetaInfo.setMock3(2.0);
    String metaString = mockMetaInfo.toString();

    MockMetaInfo newMetaInfo = MetaInfo.parseOf(metaString, MockMetaInfo.class);
    Assert.assertNotSame(mockMetaInfo, newMetaInfo);
    Assert.assertEquals(mock0, newMetaInfo.getMock0());
    Assert.assertEquals(mock1, newMetaInfo.getMock1());
    Assert.assertEquals(mock2, newMetaInfo.isMock2());
    Assert.assertEquals(mock3, newMetaInfo.getMock3());
  }
}

class MockMetaInfo extends MetaInfo {

  private String mock0;
  private int mock1;
  private boolean mock2;
  private double mock3;

  public MockMetaInfo() {
    super(MetaType.CLIENT);
  }

  public MockMetaInfo(MetaType metaType) {
    super(metaType);
  }

  public String getMock0() {
    return mock0;
  }

  public void setMock0(String mock0) {
    this.mock0 = mock0;
  }

  public int getMock1() {
    return mock1;
  }

  public void setMock1(int mock1) {
    this.mock1 = mock1;
  }

  public boolean isMock2() {
    return mock2;
  }

  public void setMock2(boolean mock2) {
    this.mock2 = mock2;
  }

  public double getMock3() {
    return mock3;
  }

  public void setMock3(double mock3) {
    this.mock3 = mock3;
  }
}
